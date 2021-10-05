// The Cloud Functions for Firebase SDK to create Cloud Functions and set up triggers.

const functions = require('firebase-functions');

// The Firebase Admin SDK to access Firestore.
const admin = require('firebase-admin');
admin.initializeApp();

const db = admin.firestore();


exports.createNoteToOtherDevices = functions.firestore
                        .document('users/{userId}/{notes}/{noteId}')
                        .onCreate( async (snap,context)=>{
                        let data = snap.data();
                        let userId = context.params.userId;
                        let snapshot = await db.collection("users").doc(userId).collection("devices").get();
                        if(snapshot.empty){
                        return;
                        }
                        let devices = snapshot.docs.map(
                        (doc)=>{
                        return doc.id
                        });
                        let otherDeviceIds = devices.filter((id)=>id !==data.deviceId );
                        otherDeviceIds.forEach((deviceId)=>{

                        await db.collection("users").doc(userId).collection("updates").doc(deviceId).collection("notes").doc(data.id).set(data);
                        }
                       );
                        });
