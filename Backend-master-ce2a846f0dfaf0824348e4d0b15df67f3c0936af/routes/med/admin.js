var express = require('express');
var router = express.Router();
var bodyParser = require('body-parser');
var pg = require('pg');
var async = require('async');
var nodemailer = require('nodemailer');
var schedule = require('node-schedule');
const user = require('./user');
var PushNotifs = require('./PushNotifs');
router.use(bodyParser.urlencoded({ extended: false }));
router.use(bodyParser.json());


const connectionString = process.env.DATABASE_URL || 'postgres://postgres:southwestcorner@198.50.214.186:5432/postgres';
pg.defaults.ssl = true;

module.exports = {
  addMed: function(req, res) {
    const results = [];
    //Ensure json includes required payload
    if ((typeof req.body.name === 'undefined') || (typeof req.body.type === 'undefined')
        || (typeof req.body.dosage === 'undefined') || (typeof req.body.addNote === 'undefined')
        || (typeof req.body.alarmNum === 'undefined') || (typeof req.body.alarms === 'undefined')
        || (typeof req.body.user === 'undefined')) {
      return res.status(400).json({success: false, message: 'One or more required key-value pairs missing in json body'});
    }
    pg.connect(connectionString, (err, client, done) => {
      // Handle connection errors
      if(err) {
        done();
        console.log(err);
        return res.status(500).json({success: false, data: err});
      }
      var query = client.query('insert into medication (email, name, type, dosage, addnote, alarmnum, currentstreak, longeststreak) values($1,$2,$3,$4,$5,$6,$7,0,0) RETURNING id;',[req.body.user, req.body.name, req.body.type, req.body.dosage, req.body.addNote, req.body.alarmNum]);
      query.on('error', (err) =>{
        console.log(err);
        res.status(500).json({success: false, error: err});
        done();
      });
      query.on('row', (row) => {
        results.push(row);
      });
      query.on('end', () =>{
        //Create the scheduled alarms for pushing
        const rules = [];
        for (i = 0; i < req.body.alarmNum; i++){
          rules[i] = new schedule.RecurrenceRule();
          rules[i].dayOfWeek = req.body.alarms[i].dayOfWeek;
          rules[i].hour = req.body.alarms[i].hour;
          rules[i].minute = req.body.alarms[i].minute;
          //don't fully understand if this will work but hey!
          schedule.scheduleJob(rules[i], function(id, email){
            PushNotifs(id, email);
          }.bind(null, results[0].id, req.body.user));

          var query2 = client.query('INSERT INTO alarms (email, med_id, hour, minute, days) values($1,$2,$3,$4,$5);', [req.body.user, results[0].id, req.body.alarms[i].hour, req.body.alarms[i].minute,  req.body.alarms[i].dayOfWeek]);
          query2.on('error', (err) =>{
            console.log(err);
            res.status(500).json({success: false, error: err});
            done();
          });
        }
        done();
        res.status(200).json({success: true, message: "Medication added successfully"});
      });
    });
  },
  getMeds: function(req, res) {
    var medicationslist = [];
    var alarms = [];
    pg.connect(connectionString, (err, client, done) => {
      // Handle connection errors
      if(err) {
        done();
        console.log(err);
        return res.status(500).json({success: false, data: err});
      }

      var query = client.query('SELECT * FROM medication WHERE email=($1);',[req.params.email]);
      query.on('error', (err) =>{
        console.log(err);
        res.status(500).json({success: false, error: err});
        done();
      });
      query.on('row', (row) => {
        medicationslist.push(row);
      });
      query.on('end', () =>{
        var query2 = client.query('SELECT * FROM alarms WHERE email=($1);',[req.params.email]);
        query2.on('error', (err) =>{
          console.log(err);
          res.status(500).json({success: false, error: err});
          done();
        });
        query2.on('row', (row) => {
          alarms.push(row);
        });
        query2.on('end', () =>{
          done();
          var k;
          for (i = 0; i < medicationslist.length; i++){
            medicationslist[i].alarms = [];
            k = 0;
            for (j = 0; j < alarms.length; j++){
              if (medicationslist[i].id === alarms[j].med_id){
                medicationslist[i].alarms[k] = alarms[j];
                medicationslist[i].alarms[k].days = JSON.parse(alarms[j].days);
                k++;
              }
            }
          }
          res.status(200).send({medications: medicationslist.length, medicationslist});
        });
      });
    });
  },
  delete: function(req, res) {
    const results = [];

    if (typeof req.body.id === 'undefined' || typeof req.body.user === 'undefined') {
      return res.status(400).json({success: false, message: 'Body must contain an id: <value> and/or user: <value>pair'});
    }

    pg.connect(connectionString, (err, client, done) => {
      // Handle connection errors
      if(err) {
        done();
        console.log(err);
        return res.status(500).json({success: false, data: err});
      }
      var query = client.query('DELETE FROM alarms where med_id=($1) AND email=($2);',[req.body.id, req.body.user]);
      query.on('error', (err) =>{
        console.log(err);
        res.status(500).json({success: false, error: err});
        done();
      });
      query.on('end', () =>{
        var query2 = client.query('DELETE FROM medication where id=($1) AND email=($2);',[req.body.id, req.body.user]);
        query2.on('error', (err) =>{
          console.log(err);
          res.status(500).json({success: false, error: err});
          done();
        });
        done();
        res.status(200).send({success: true, message: "Medication deleted succesfully"});
      });
    });
  },
  updateMed: function (req, res) {
    const results = [];
    var rname, rtype, rdosage, addNote;

    if (typeof req.body.id === 'undefined' || typeof req.body.user === 'undefined') {
      return res.status(400).json({success: false, message: 'Body must contain an id: <value> and email: <value> pair'});
    }

    pg.connect(connectionString, (err, client, done) => {
      // Handle connection errors
      if(err) {
        done();
        console.log(err);
        return res.status(500).json({success: false, data: err});
      }
      var query = client.query('Select * FROM medication where id=($1);',[req.body.id]);
      query.on('row', (row) => {
        results.push(row);
      });
      query.on('end', () =>{
        if (typeof req.body.name === 'undefined') {
          rname = results[0].name;
        } else {
          rname = req.body.name;
        }
        if (typeof req.body.type === 'undefined') {
          rtype = results[0].type;
        } else {
          rtype = req.body.type;
        }
        if (typeof req.body.dosage === 'undefined') {
          rdosage = results[0].dosage;
        } else {
          rdosage = Date(req.body.dosage);
        }
        if (typeof req.body.addNote === 'undefined') {
          addNote = results[0].addNote;
        } else {
          addNote = req.body.addNote;
        }

        var query2 = client.query('update medication set name=($1), type=($2), dosage=($3), addnote=($4) where id=($5) AND email=($6);',[rname, rtype, rdosage, addNote, req.body.id, req.body.user]);
        query2.on('error', (err) =>{
          console.log(err);
          res.status(500).json({success: false, error: err});
          done();
        });
        query2.on('row', (row) => {
          results.push(row);
        });
        done();
        res.status(200).send({success: true, message: "Medication updated succesfully"});
      });
    });
  }
}
