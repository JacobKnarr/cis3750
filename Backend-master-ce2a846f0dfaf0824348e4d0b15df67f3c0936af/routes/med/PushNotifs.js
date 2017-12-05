var express = require('express');
var router = express.Router();
var bodyParser = require('body-parser');
var pg = require('pg');
var async = require('async');
var nodemailer = require('nodemailer');
var schedule = require('node-schedule');
var gcm = require('node-gcm');

router.use(bodyParser.urlencoded({
    extended: false
}));
router.use(bodyParser.json());


const connectionString = process.env.DATABASE_URL || 'postgres://postgres:southwestcorner@198.50.214.186:5432/postgres';
pg.defaults.ssl = true;

var sender = new gcm.Sender('AIzaSyBoGf4udiz6sgs1CK4kc4DZVVjHdrGzSpU');

function PushNotifs(id, email) {
    pg.connect(connectionString, (err, client, done) => {
        email = email.replace(/'/g, "");
        var regIdResults = [];
        //this is scheduled to happen?
        var regIdQuery = client.query('select regid from users where email=($1)', [email]);
        regIdQuery.on('error', (err) => {
            console.log(err);
        });
        regIdQuery.on('row', (row) => {
            regIdResults.push(row);
        });
        regIdQuery.on('end', () => {
            var regTokens = []
            // console.log(regIdResults[0].regid);
            console.log(regIdResults);
            if (typeof regIdResults[0] === 'undefined') {
                console.log("undefined regID");
            } else {
                regTokens.push(regIdResults[0].regid)
                regIdResults = [];
                regIdQuery = client.query('select * from medication where id=($1)', [id]);
                regIdQuery.on('error', (err) => {
                    console.log(err);
                });
                regIdQuery.on('row', (row) => {
                    regIdResults.push(row);
                });
                regIdQuery.on('end', () => {
                    // console.log(regTokens);
                    // console.log("END 2");
                    // console.log("***********");
                    // console.log(regTokens.regId);
                    // console.log("***********");
                    // console.log()
                    var message = new gcm.Message({
                        collapseKey: 'demo',
                        priority: 'high',
                        contentAvailable: true,
                        delayWhileIdle: true,
                        timeToLive: 3,
                        data: {
                            medication_id: id,
                            user: email,
                            name: regIdResults[0].name,
                            type: regIdResults[0].type,
                            dosage: regIdResults[0].dosage,
                            notification: {
                                title: regIdResults[0].name + " Reminder!",
                                icon: "ic_launcher",
                                body: "It's time to take " + regIdResults[0].name + "! " + regIdResults[0].type + ", " + regIdResults[0].dosage + "mg"
                            }
                        }
                    });
                    // console.log(regTokens);
                    // consolge.log(message);
                    sender.send(message, {
                        registrationTokens: regTokens
                    }, function (err, response) {
                        if (err) console.error(err);
                        else console.log(response);
                    });
                });
            }
        });

    });
}

module.exports = PushNotifs;