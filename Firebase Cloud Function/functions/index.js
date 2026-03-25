const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { setGlobalOptions } = require("firebase-functions/v2");
const admin = require("firebase-admin");

admin.initializeApp();

setGlobalOptions({ region: "asia-southeast1" });

exports.sendNewProductNotification = onDocumentCreated("products/{productId}", async (event) => {

    const snap = event.data;
    if (!snap) {
        console.log("No data found!");
        return;
    }

    const newProduct = snap.data();
    const productId = event.params.productId;

    const message = {
      notification: {
        title: "🔥 New Product Added!",
        body: `Check out the new ${newProduct.brand} ${newProduct.model} for just Rs. ${newProduct.price}!`,
      },
      data: {
        click_action: "FLUTTER_NOTIFICATION_CLICK", 
        productId: productId,
      },
      topic: "new_products" 
    };

    try {
      const response = await admin.messaging().send(message);
      console.log("✅ Notification sent successfully:", response);
    } catch (error) {
      console.error("❌ Error sending notification:", error);
    }
});