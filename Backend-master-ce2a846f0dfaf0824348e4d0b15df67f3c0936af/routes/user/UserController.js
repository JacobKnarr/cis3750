var express = require('express');
var router = express.Router();
var bodyParser = require('body-parser');
var pg = require('pg');
var async = require('async');
var nodemailer = require('nodemailer');

var VerifyToken = require(__root + '/routes/auth/VerifyToken');
var VerifyPermission = require('../auth/VerifyPermission')

router.use(bodyParser.urlencoded({ extended: false }));
router.use(bodyParser.json());

const connectionString = process.env.DATABASE_URL || 'postgres://postgres:southwestcorner@198.50.214.186:5432/postgres';
pg.defaults.ssl = true;

var config = require('../../config');

//Endpoint to delete a User's acct by said User
router.delete('/accdel', VerifyToken, function(req, res, next) {
  const results = [];
  pg.connect(connectionString, function(err, client, done){
    if(err){
      done();
      console.log(err);
      return res.status(500).json({success: false, error: err});
    }
    var query = client.query("DELETE FROM alarms where email=($1);", [req.email]);
    var query = client.query("DELETE FROM medication where email=($1);", [req.email]);
    var query = client.query("DELETE FROM USERS where email=($1);", [req.email]);
    query.on('error', (err)=>{
      console.log(err);
      res.status(500).json({success: false, error: err});
    });
    query.on('row', (row)=>{
      results.push(row);
    });
    query.on('end', () =>{
      done();
      res.status(200).json({authenticated: false, success: true, data: 'User succesfully deleted'});
    });
  });
});

function checkUndef(str) {
  return new Promise(function(accept, reject) {
    if (typeof str !== 'undefined') {
      accept(1)
    }
  })
}

//Endpoint to update User details
router.put('/update', VerifyToken, function(req, res, next) {
  const results = [];
  //Select * user then compare body to results. Update with data.fname || results.fname
  //Can add more booleans here for new fields
  pg.connect(connectionString, function(err, client, done){
    console.log();
    if(err){
      done();
      console.log(err);
      return res.status(500).json({success: false, error: err});
    }

    var newfname = req.body.fname;
    console.log("fname: "+newfname);
    var newlname = req.body.lname;
    console.log("lname: "+newlname);

    //Get the user with the email
    var query = client.query("select * from USERS where email=($1);", [req.email]);
    query.on('row', (row)=>{
      results.push(row);
    })
    query.on('end', () => {
      if (typeof req.body.fname === 'undefined') {
        newfname = results[0].fname;
      } else {
        newfname = req.body.fname;
      }

      if (typeof req.body.lname === 'undefined') {
        newlname = results[0].lname;
      } else {
        newlname = req.body.lname;
      }

      var query_update = client.query("update users set fname=($1), lname=($2) where email=($3);", [newfname, newlname, req.email]);
      query_update.on('error', (err)=>{
        console.log(err);
        res.status(500).json({success: false, error: err});
      });
      query_update.on('row', (row)=>{
        results.push(row);
      });
    })

    done();
    res.status(200).json({authenticated: true, success: true, data: 'User info successfully updated'});
  });
});

router.get('/allUsers', [VerifyToken, VerifyPermission('sw')], function(req,res,next){
  var results = [];
  pg.connect(connectionString, function (err, client, done){
    if (err) {
      done();
      console.log(err);
      return res.status(500).json({
        success: false,
        data: err
      });
    }
    var query = client.query('select * from users;');
    query.on('error', (err)=>{
      console.log(err);
      res.status(500).json({
        success: false,
        error: err
      });
    });
    query.on('row', (row) =>{
      results.push(row);
    });
    query.on('end', ()=>{
      var users = [];
      for(var result in results){
        var user = {};
        console.log(results[result]);
        user.email = results[result].email;
        user.fname = results[result].fname;
        user.lname = results[result].lname;
        user.picpath = results[result].picpath;
        users.push(user);
      }
      res.status(200).json(users);
    });
  });
});

router.get('/usersFor/:swemail', [VerifyToken, VerifyPermission('sw')], function(req, res, next){
  var results = [];
  pg.connect(connectionString, function(err, client, done){
    if (err) {
      done();
      console.log(err);
      return res.status(400).json({
        success: false,
        data: err
      });
    }
    var query = client.query('select u.email, u.fname, u.lname from supportrelation sr, users u where sr.supportemail=($1) and sr.clientemail=u.email;', [req.params.swemail]);
    query.on('error', (err)=>{
      console.log(err);
      res.status(400).json({success: false, error: err});
    });
    query.on('row', (row) =>{
      results.push(row);
    });
    query.on('end', ()=>{
      res.status(200).json({success: true, userData: results});
    });
  });
});

router.post('/assignUser', [VerifyToken, VerifyPermission('sw')], function(req, res, next){
  var results = [];
  var clcUser = req.body.clcUser;
  //set the sw to be assigned to based on the request, or if not included assume it will be assigned to the sw making the request
  var sw = req.body.sw || req.email;
  pg.connect(connectionString, function(err, client, done){
    if (err) {
      done();
      console.log(err);
      return res.status(400).json({
        success: false,
        data: err
      });
    }
    var query = client.query('insert into supportrelation (clientemail, supportemail) values (($1), ($2));', [clcUser, sw]);
    query.on('error', (err) =>{
      console.log(err);
      res.status(400).json({success: false, error: err});
    });
    query.on('row', (row) =>{
      results.push(row);
    });
    query.on('end', () =>{
      res.status(201).json({success:true});
    });

  });
});


//MEDICATION STREAK DATA

module.exports = router;
