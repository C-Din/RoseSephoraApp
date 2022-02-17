'use strict'
const functions = require('firebase-functions');
const admin = require('firebase-admin');
//Object.values = require('object.values');
admin.initializeApp(functions.config().firebase);
exports.sendNotification = functions.database.ref('/publications/{pub_id}').onWrite(event => {
  const snapshot = event.data;
  // Only send a notification when a new message has been created.
  if (snapshot.previous.val()) {
    return;
  }
  const pub_id = event.params.pub_id;

  const pub_content=admin.database().ref(`publications/${pub_id}`).once('value');
  return pub_content.then(pubResult =>{
    const msg_title=pubResult.val().title;
    const user_id=pubResult.val().userId;
    console.log('Le titre du post est ',pub_title);
     console.log('Vous avez un nouveau post : ', pub_id);
      const payload={

        data : {
          title:"Nouveau post",
          body: pub_title,
          pubid : pub_id,
          userid : user_id

        }
    };




 const getDeviceTokensPromise = admin.database().ref('/users').once('value');
 return Promise.all([getDeviceTokensPromise, pub_title]).then(results => {


   const tokensSnapshot = results[0];
    const msgi = results[1];

  if (!tokensSnapshot.hasChildren()) {
      return console.log('There are no notification tokens to send to.');
    }
    console.log('There are', tokensSnapshot.numChildren(), 'tokens to send notifications to.');
    console.log("tokenslist",tokensSnapshot.val());
   const tokens= Object.keys(tokensSnapshot.val()).map(e => tokensSnapshot.val()[e]);
   //var values = Object.keys(o).map(e => obj[e])


     return admin.messaging().sendToDevice(tokens, payload).then(response => {
      // For each message check if there was an error.
      const tokensToRemove = [];
      response.results.forEach((result, index) => {
        const error = result.error;
        if (error) {
          console.error('Failure sending notification to', tokens[index], error);
          // Cleanup the tokens who are not registered anymore.

        }
      });
      return Promise.all(tokensToRemove);
    });

});
});
});