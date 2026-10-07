const {setGlobalOptions} = require("firebase-functions/v2");
const {onDocumentCreated} = require("firebase-functions/v2/firestore");
const {logger} = require("firebase-functions");
const {initializeApp} = require("firebase-admin/app");
const {getFirestore, FieldValue} = require("firebase-admin/firestore");
const {getMessaging} = require("firebase-admin/messaging");

initializeApp();

setGlobalOptions({region: "us-east1", maxInstances: 3});

exports.sendMessageNotification = onDocumentCreated(
    "chats/{chatId}/messages/{messageId}",
    async (event) => {
      const message = event.data.data();
      const chatId = event.params.chatId;

      const [uidA, uidB] = chatId.split("_");
      const recipientId = message.senderId === uidA ? uidB : uidA;
      const preview = message.imageUrl ? "Foto" : message.text;

      const db = getFirestore();

      // 1. Resumen del chat para la lista de conversaciones
      try {
        await db.collection("chats").doc(chatId).set({
          participants: [uidA, uidB],
          lastMessage: preview.substring(0, 100),
          lastMessageTime: message.timestamp || FieldValue.serverTimestamp(),
          lastSenderId: message.senderId,
          unread: {[recipientId]: FieldValue.increment(1)},
        }, {merge: true});
      } catch (error) {
        logger.error("Error al actualizar el resumen del chat", error);
      }

      // 2. Notificación push al destinatario
      const tokenRef = db.collection("fcmTokens").doc(recipientId);
      const tokenDoc = await tokenRef.get();
      if (!tokenDoc.exists) {
        logger.info("El destinatario no tiene token", {recipientId});
        return;
      }

      const body = message.imageUrl ? "Te envió una foto" : message.text;

      try {
        await getMessaging().send({
          token: tokenDoc.get("token"),
          data: {
            senderId: message.senderId,
            senderName: message.senderName,
            body: body.substring(0, 150),
          },
          android: {priority: "high"},
        });
        logger.info("Notificación enviada", {recipientId});
      } catch (error) {
        logger.error("Error al enviar la notificación", error);

        if (error.code === "messaging/registration-token-not-registered" ||
            error.code === "messaging/invalid-registration-token") {
          await tokenRef.delete();
        }
      }
    },
);