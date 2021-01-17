var koa = require('koa');
var app = module.exports = new koa();
const server = require('http').createServer(app.callback());
const WebSocket = require('ws');
const wss = new WebSocket.Server({server});
const Router = require('koa-router');
const cors = require('@koa/cors');
const bodyParser = require('koa-bodyparser');

app.use(bodyParser());

app.use(cors());

app.use(middleware);

function middleware(ctx, next) {
  const start = new Date();
  return next().then(() => {
    const ms = new Date() - start;
    console.log(`${start.toLocaleTimeString()} ${ctx.request.method} ${ctx.request.url} ${ctx.response.status} - ${ms}ms`);
  });
}

const getRandomInt = (min, max) => {
  min = Math.ceil(min);
  max = Math.floor(max);
  return Math.floor(Math.random() * (max - min)) + min;
};

const names = ['Fareies',
  'Fair Fares',
  'Cabme',
  'Cabby Shack',
  'Cool Cab',
  'Quick Cab',
  'The Party Van',
  'Bookar',
  'DeTaxing',
  'Bookit',
  'City Transfers',
  'Sure Way',
  'MaxiTaxi'];
const statuses = ['new', 'working', 'damaged', 'private'];
const drivers = ['John', 'Joe', 'Jim', 'Jone', 'Joan', 'Jack'];
const colors = ['Yellow', 'Blue', 'Black', 'Green', 'White', 'Red'];
const cabs = [];

for (let i = 0; i < 50; i++) {
  cabs.push({
    id: i + 1,
    name: names[getRandomInt(0, names.length)],
    status: statuses[getRandomInt(0, statuses.length)],
    size: getRandomInt(2, 20),
    driver: drivers[getRandomInt(0, drivers.length)],
    color: colors[getRandomInt(0, colors.length)],
    capacity: getRandomInt(0, 400)
  });
}

const router = new Router();

router.get('/all', ctx => {
  ctx.response.body = cabs;
  ctx.response.status = 200;
});

router.get('/colors', ctx => {
  ctx.response.body = [...new Set(cabs.map(obj => obj.color))];
  ctx.response.status = 200;
});

router.get('/cabs/:color', ctx => {
  const headers = ctx.params;
  const color = headers.color;
  if (typeof color !== 'undefined') {
    ctx.response.body = cabs.filter(obj => obj.color == color);
    ctx.response.status = 200;
  } else {
    console.log("Missing or invalid: color!");
    ctx.response.body = {text: 'Missing or invalid: color!'};
    ctx.response.status = 404;
  }
});

router.get('/my/:driver', ctx => {
  const headers = ctx.params;
  const driver = headers.driver;
  if (typeof driver !== 'undefined') {
    ctx.response.body = cabs.filter(obj => obj.driver == driver);
    ctx.response.status = 200;
  } else {
    console.log("Missing or invalid: driver!");
    ctx.response.body = {text: 'Missing or invalid: driver!'};
    ctx.response.status = 404;
  }
});


const broadcast = (data) =>
  wss.clients.forEach((client) => {
    if (client.readyState === WebSocket.OPEN) {
      client.send(JSON.stringify(data)); 
    }
  });

router.post('/cab', ctx => {
  // console.log("ctx: " + JSON.stringify(ctx));
  const headers = ctx.request.body;
  // console.log("body: " + JSON.stringify(headers));
  const name = headers.name;
  const size = headers.size;
  const driver = headers.driver;
  const color = headers.color;
  const capacity = headers.capacity;
  if (typeof name !== 'undefined' && typeof driver !== 'undefined' && typeof size !== 'undefined'
    && color !== 'undefined' && capacity !== 'undefined') {
    const index = cabs.findIndex(obj => obj.name == name && obj.driver == driver);
    if (index !== -1) {
      console.log("Cab already exists!");
      ctx.response.body = {text: 'Cab already exists!'};
      ctx.response.status = 404;
    } else {
      let maxId = Math.max.apply(Math, cabs.map(function (obj) {
        return obj.id;
      })) + 1;
      let obj = {
        id: maxId,
        name,
        status: 'new',
        size,
        driver,
        color,
        capacity
      };
      // console.log("created: " + JSON.stringify(name));
      cabs.push(obj);
      broadcast(obj);
      ctx.response.body = obj;
      ctx.response.status = 200;
    }
  } else {
    console.log("Missing or invalid fields!");
    ctx.response.body = {text: 'Missing or invalid fields!'};
    ctx.response.status = 404;
  }
});

router.del('/cab/:id', ctx => {
  // console.log("ctx: " + JSON.stringify(ctx));
  const headers = ctx.params;
  // console.log("body: " + JSON.stringify(headers));
  const id = headers.id;
  if (typeof id !== 'undefined') {
    const index = cabs.findIndex(obj => obj.id == id);
    if (index === -1) {
      console.log("No cab with id: " + id);
      ctx.response.body = {text: 'Invalid cab id'};
      ctx.response.status = 404;
    } else {
      let obj = cabs[index];
      // console.log("deleting: " + JSON.stringify(obj));
      cabs.splice(index, 1);
      ctx.response.body = obj;
      ctx.response.status = 200;
    }
  } else {
    console.log("Missing or invalid fields!");
    ctx.response.body = {text: 'Id missing or invalid'};
    ctx.response.status = 404;
  }
});

app.use(router.routes());
app.use(router.allowedMethods());

server.listen(1957);