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

var PushNotifs = require('./PushNotifs');
const connectionString = process.env.DATABASE_URL || 'postgres://postgres:southwestcorner@198.50.214.186:5432/postgres';
pg.defaults.ssl = true;
var sender = new gcm.Sender('AIzaSyBoGf4udiz6sgs1CK4kc4DZVVjHdrGzSpU');

module.exports = {
  addMed: function (req, res) {
    const results = [];
    //Ensure json includes required payload
    if ((typeof req.body.name === 'undefined') || (typeof req.body.type === 'undefined') ||
      (typeof req.body.dosage === 'undefined') || (typeof req.body.addNote === 'undefined') ||
      (typeof req.body.alarmNum === 'undefined') || (typeof req.body.alarms === 'undefined')) {
      return res.status(400).json({
        success: false,
        message: 'One or more required key-value pairs missing in json body'
      });
    }
    pg.connect(connectionString, (err, client, done) => {
      // Handle connection errors
      if (err) {
        done();
        console.log(err);
        return res.status(500).json({
          success: false,
          data: err
        });
      }
      var query = client.query('insert into medication (email, name, type, dosage, addnote, alarmnum, currentstreak, longeststreak) values($1,$2,$3,$4,$5,$6,0,0) RETURNING id;', [req.email, req.body.name, req.body.type, req.body.dosage, req.body.addNote, req.body.alarmNum]);
      query.on('error', (err) => {
        console.log(err);
        res.status(500).json({
          success: false,
          error: err
        });
        done();
      });
      query.on('row', (row) => {
        results.push(row);
      });
      query.on('end', () => {
        //Create the scheduled alarms for pushing
        const rules = [];
        for (i = 0; i < req.body.alarmNum; i++) {
          rules[i] = new schedule.RecurrenceRule();
          rules[i].dayOfWeek = req.body.alarms[i].dayOfWeek;
          rules[i].hour = req.body.alarms[i].hour;
          rules[i].minute = req.body.alarms[i].minute;
          //don't fully understand if this will work but hey!
          schedule.scheduleJob(results[i], function (id, email) {
              PushNotifs(id, email);
            }
            .bind(null, results[0].id, req.email));

          var query2 = client.query('INSERT INTO alarms (email, med_id, hour, minute, days) values($1,$2,$3,$4,$5);', [req.email, results[0].id, req.body.alarms[i].hour, req.body.alarms[i].minute, JSON.stringify(req.body.alarms[i].dayOfWeek)]);
          query2.on('error', (err) => {
            console.log(err);
            res.status(500).json({
              success: false,
              error: err
            });
            done();
          });
        }
        done();
        res.status(200).json({
          success: true,
          message: "Medication added successfully"
        });
      });
    });
  },
  getMeds: function (req, res) {
    var medicationslist = [];
    var alarms = [];
    pg.connect(connectionString, (err, client, done) => {
      // Handle connection errors
      if (err) {
        done();
        console.log(err);
        return res.status(500).json({
          success: false,
          data: err
        });
      }

      var query = client.query('SELECT * FROM medication WHERE email=($1);', [req.email]);
      query.on('error', (err) => {
        console.log(err);
        res.status(500).json({
          success: false,
          error: err
        });
        done();
      });
      query.on('row', (row) => {
        medicationslist.push(row);
      });
      query.on('end', () => {
        var query2 = client.query('SELECT * FROM alarms WHERE email=($1);', [req.email]);
        query2.on('error', (err) => {
          console.log(err);
          res.status(500).json({
            success: false,
            error: err
          });
          done();
        });
        query2.on('row', (row) => {
          alarms.push(row);
        });
        query2.on('end', () => {
          done();
          var k;
          for (i = 0; i < medicationslist.length; i++) {
            medicationslist[i].alarms = [];
            k = 0;
            for (j = 0; j < alarms.length; j++) {
              if (medicationslist[i].id === alarms[j].med_id) {
                medicationslist[i].alarms[k] = alarms[j];
                medicationslist[i].alarms[k].days = JSON.parse(alarms[j].days);
                k++;
              }
            }
          }
          res.status(200).send({
            medications: medicationslist.length,
            medicationslist
          });
        });
      });
    });
  },
  deleteMeds: function (req, res) {
    const results = [];
    console.log(req.body);
    if (typeof req.body.id === 'undefined') {
      return res.status(400).json({
        success: false,
        message: 'body must contain an id: <value> pair'
      });
    }

    pg.connect(connectionString, (err, client, done) => {
      // Handle connection errors
      if (err) {
        done();
        console.log(err);
        return res.status(500).json({
          success: false,
          data: err
        });
      }
      var query = client.query('DELETE FROM alarms where med_id=($1) AND email=($2);', [req.body.id, req.email]);
      query.on('error', (err) => {
        console.log(err);
        res.status(500).json({
          success: false,
          error: err
        });
        done();
      });
      query.on('end', () => {
        var query2 = client.query('DELETE FROM medication where id=($1) AND email=($2);', [req.body.id, req.email]);
        query2.on('error', (err) => {
          console.log(err);
          res.status(500).json({
            success: false,
            error: err
          });
          done();
        });
        done();
        res.status(200).send({
          success: true,
          message: "Medication deleted succesfully"
        });
      });
    });
  },
  updateMed: function (req, res) {
    const results = [];
    var rname, rtype, rdosage, addNote;

    if (typeof req.body.id === 'undefined') {
      return res.status(400).json({
        success: false,
        message: 'Body must contain an id: <value> pair'
      });
    }

    pg.connect(connectionString, (err, client, done) => {
      // Handle connection errors
      if (err) {
        done();
        console.log(err);
        return res.status(500).json({
          success: false,
          data: err
        });
      }
      var query = client.query('Select * FROM medication where id=($1);', [req.body.id]);
      query.on('row', (row) => {
        results.push(row);
      });
      query.on('end', () => {
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

        var query2 = client.query('update medication set name=($1), type=($2), dosage=($3), addnote=($4) where id=($5) AND email=($6);', [rname, rtype, rdosage, addNote, req.body.id, req.email]);
        query2.on('error', (err) => {
          console.log(err);
          res.status(500).json({
            success: false,
            error: err
          });
          done();
        });
        query2.on('row', (row) => {
          results.push(row);
        });
        done();
        res.status(200).send({
          success: true,
          message: "Medication updated succesfully"
        });
      });
    });
  },

  streakUpdate: function (req, res) {
    const results = [];
    var name, email, currentstreak, longeststreak;
    id = req.body.id;
    email = req.email;

    if (typeof req.body.id === 'undefined') {
      return res.status(400).json({
        success: false,
        message: 'Body must contain a id: <value> pair'
      });
    }

    pg.connect(connectionString, (err, client, done) => {
      // Handle connection errors
      if (err) {
        done();
        console.log(err);
        return res.status(500).json({
          success: false,
          data: err
        });
      }
      var query = client.query('Select * FROM medication where email=($1) AND id=($2);', [email, id]);
      query.on('row', (row) => {
        results.push(row);
      });
      query.on('end', () => {
        currentstreak = results[0].currentstreak;
        longeststreak = results[0].longeststreak;
        if (req.body.increment == 1) {
          currentstreak += 1;
        } else if (req.body.increment == 0) {
          currentstreak = 0;
        }
        if (currentstreak > longeststreak) {
          longeststreak = currentstreak;
        }

        var query2 = client.query('update medication set currentstreak=($1), longeststreak=($2) where email=($3) AND id=($4);', [currentstreak, longeststreak, email, id]);
        query2.on('error', (err) => {
          console.log(err);
          res.status(500).json({
            success: false,
            error: err
          });
          done();
        });
        query2.on('row', (row) => {
          results.push(row);
        });
        done();
        res.status(200).send({
          success: true,
          message: "Streak updated succesfully"
        });
      });
    });
  }
}