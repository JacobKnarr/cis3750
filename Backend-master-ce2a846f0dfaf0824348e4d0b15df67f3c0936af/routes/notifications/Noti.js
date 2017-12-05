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