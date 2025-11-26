import express from 'express';
import cookieParser from 'cookie-parser';
import morgan from 'morgan';
import cors from 'cors';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';
import indexRouter from './routes/index.mjs';
import rfs from 'rotating-file-stream';

const app = express();

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const accessLogStream = rfs.createStream('access.log', {
  interval: '1d', // rotate daily
  path: join(__dirname, 'log')
});

app.use(morgan('combined', { stream: accessLogStream }));


app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());

app.use('/', indexRouter);

// error handler
app.use((err, req, res, next) => {
  // set locals, only providing error in development
  console.log("ERROR: " + JSON.stringify(err));
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.json({'error' : err});
});

export default app;
