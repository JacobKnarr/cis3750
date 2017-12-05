var express = require('express');
var router = express.Router();
var pg = require('pg');
var request = require('superagent');
var jwksRsa = require('jwks-rsa');
var jwt = require('express-jwt');

const connectionString = process.env.DATABASE_URL || 'postgres://postgres:southwestcorner@198.50.214.186:5432/postgres';
pg.defaults.ssl = true;
/* GET home page. */


module.exports = router;
