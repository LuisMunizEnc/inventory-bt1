import "@testing-library/jest-dom"
global.TextEncoder = require('util').TextEncoder;
global.TextDecoder = require('util').TextDecoder;
import "whatwg-fetch";
import {
  BroadcastChannel
} from 'worker_threads'
import { TransformStream } from "stream/web";

Reflect.set(globalThis, 'BroadcastChannel', BroadcastChannel)

if (typeof global.TransformStream === "undefined") {
  Object.defineProperty(global, "TransformStream", {
    value: TransformStream,
    writable: true,
  });
}
