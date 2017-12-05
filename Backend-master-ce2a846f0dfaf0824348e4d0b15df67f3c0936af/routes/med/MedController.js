var express = require('express');
var router = express.Router();
var bodyParser = require('body-parser');
var pg = require('pg');
var async = require('async');
var nodemailer = require('nodemailer');
const user = require('./user');
const admin = require('./admin');
var VerifyToken = require(__root + '/routes/auth/VerifyToken');
var VerifyPermission = require(__root + '/routes/auth/VerifyPermission');
router.use(bodyParser.urlencoded({
  extended: false
}));
router.use(bodyParser.json());
//var User = require('./User'); TESTING BRANCH

const connectionString = process.env.DATABASE_URL || 'postgres://postgres:southwestcorner@198.50.214.186:5432/postgres';
pg.defaults.ssl = true;

//TODO: Make endpoints go to functions based on scope. have functions in sepereate .js files
//add new medication by a user for said user
router.post('/add', VerifyToken, function(req, res, next) {
  if (req.scope == 'sw') {

  } else if (req.scope == 'admin') {
    admin.addMed(req, res);
  } else {
    user.addMed(req, res);
  }
});

//Get users' medications for said user
router.get('/meds', VerifyToken, function(req, res, next) {
  user.getMeds(req, res);
});
//Get a specific users' meds for admin
router.get('/usermeds/:email', [VerifyToken, VerifyPermission('admin')], function(req, res, next) {
  admin.getMeds(req, res);
});

//DELETE a users' medication for said user
router.post('/delete', VerifyToken, function(req, res, next) {
  if (req.scope == 'sw') {

  } else if (req.scope == 'admin'){
    admin.delete(req, res);
  } else {
    user.deleteMeds(req, res);
  }
});

router.put('/update', VerifyToken, function(req, res, next) {
  if (req.scope == 'sw') {

  } else if (req.scope == 'admin') {
    admin.updateMed(req, res);
  } else {
    user.updateMed(req, res);
  }
});

router.put('/streakupdate', VerifyToken, function(req, res, next) {
  //A value of 1 for increment increases the streak, a value of 0 resets the streak
    user.streakUpdate(req, res);
});


router.get('/streakInfo/:medication', VerifyToken, function (req, res, next) {
  var results = [];
  pg.connect(connectionString, (err, client, done) => {
    // Handle connection errors
    console.log(req.email);
    console.log(req.params.medication);
    if (err) {
      done();
      console.log(err);
      return res.status(500).json({
        success: false,
        data: err
      });
    }
    var query = client.query('select currentstreak, longeststreak from medication where email=($1) and type=($2)', [req.email, req.params.medication]);
    query.on('error', (err) => {
      console.log(err);
      res.status(400).json({
        success: false,
        error: err
      });
    });
    query.on('row', (row) => {
      results.push(row);
    });
    query.on('end', () => {
      console.log(results);
      if (results.length < 1) {
        res.status(404).json({
          success: false,
          error: "Medication not found"
        });
      } else {
        var result = results[0];
        console.log(result);
        res.status(200).json({
          success: true,
          currentstreak: result.currentstreak,
          longeststreak: result.longeststreak
        });
      }
    });

  });
});

module.exports = router;
