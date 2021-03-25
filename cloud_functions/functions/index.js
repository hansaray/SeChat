const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp();

exports.newMessageNotification=functions.database.ref("/messages/{receiver_ID}/{sender_ID}/{newMessage_ID}").onCreate((data,context)=>{
   const receiverID=context.params.receiver_ID;
   const senderID= context.params.sender_ID;
   const newMessageID=context.params.newMessage_ID;

   const receiverToken = admin.database().ref(`/users/${receiverID}/fcm_token`).once('value');
   const senderName = admin.database().ref(`/users/${senderID}/nickname`).once('value');
   const lastMessage = admin.database().ref(`/messages/${receiverID}/${senderID}/${newMessageID}`).once('value');
   const message_Type = admin.database().ref(`/messages/${receiverID}/${senderID}/${newMessageID}/type`).once('value');
   const mute_check = admin.database().ref(`/messagesFastAccess/${receiverID}/${senderID}/mute`).once('value');
   return receiverToken.then(result=>{
     const userToken = result.val();
     return senderName.then(result=>{
       const userName = result.val();
       return lastMessage.then(result=>{
         const last_message = result.child('message').val();
         const last_message_ID = result.child('user_id').val();
         return message_Type.then(result=>{
           const messageType = result.val();
           return mute_check.then(result=>{
             const muteCheck = result.val();
             var text1;
             if(messageType === "post"){
               text1 = "sent a post";
             }else if(messageType === "postAdoption"){
               text1 = "sent a post";
             }else if(messageType === "postLost"){
               text1 = "sent a post";
             }else if(messageType === "photo"){
               text1 = "sent a photo";
             }else if(messageType === "video"){
               text1 = "sent a video";
             }else if(messageType === "location"){
               text1 = "sent a location";
             }else{
               text1 = "sent a message";
             }
             var title1 = "New Message";
               if(last_message_ID == senderID){
                 if(muteCheck == null){
                   if(receiverID !== senderID){
                     const newMessage = {
                       notification : {
                         click_action : '.NavigateActivity',
                         title : `${title1}`,
                         body : `${userName} : ${text1}`,
                         icon : 'default'
                       },
                       data : {
                         clickedUserId : `${senderID}`
                       }
                     };
                     return admin.messaging().sendToDevice(userToken, newMessage).then(result=>{
                     });
                   }
                 }
               }
           });
         });
       });
     });
   });
});
