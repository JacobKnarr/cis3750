var express = require('express');
var path = require('path');
var favicon = require('serve-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');
const user = require('./routes/med/user');
var PushNotifs = require('./routes/med/PushNotifs');
var index = require('./routes/index');

global.__root   = __dirname + '/';

var pg = require('pg');
const connectionString = process.env.DATABASE_URL || 'postgres://postgres:southwestcorner@198.50.214.186:5432/postgres';
pg.defaults.ssl = true;

var app = express();
// dependencies omitted

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');

app.on('listening', function(){
  const results = [];
  pg.connect(connectionString, (err, client, done) => {
  // Handle connection errors
    if(err) {
      done();
      console.log(err);
    }
    var query = client.query('Select * from alarms');
    query.on('error', (err) =>{
      console.log(err);
      done();
    });
    query.on('row', (row) => {
          results.push(row);
    });
    query.on('end', () =>{
      //Create the scheduled alarms for pushing
      const rules = [];
      for (i = 0; i < results.length; i++){
        rules[i] = new schedule.RecurrenceRule();
        rules[i].dayOfWeek = results[i].days;
        rules[i].hour = results[i].hour;
        rules[i].minute = results[i].minute;
        //don't fully understand if this will work but hey!
        schedule.scheduleJob(rules[i], function(id, email){
          PushNotifs(id, email);
        }.bind(null, results[i].med_id, results[i].email));
      }
      done();
    });
  });
});

app.get('/api', function (req, res) {
  res.status(200).send('API works.');
});

var UserController = require(__root + './routes/user/UserController');
app.use('/api/users', UserController);

var MedController = require(__root + './routes/med/MedController');
app.use('/api/med', MedController);

var AuthController = require(__root + './routes/auth/AuthController');
app.use('/api/auth', AuthController);

var NotifactionController = require(__root + './routes/notifications/NotiController');
app.use('/api/notifications', NotifactionController);



// uncomment after placing your favicon in /public
//app.use(favicon(path.join(__dirname, 'public', 'favicon.ico')));
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', index);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  var err = new Error('Not Found');
  err.status = 404;
  next(err);
});

// error handler
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});


module.exports = app;
