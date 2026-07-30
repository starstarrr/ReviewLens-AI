import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

const BACKEND_TARGET =
  process.env.VITE_BACKEND_PROXY_TARGET ||
  "http://localhost:8080";

function createProxyConfig() {
  return {
    target: BACKEND_TARGET,
    changeOrigin: false,
    xfwd: true,
  };
}

export default defineConfig({
  plugins: [react()],

  server: {
    host: "localhost",
    port: 5173,

    proxy: {
      "/api": createProxyConfig(),
      "/github": createProxyConfig(),
      "/reviews": createProxyConfig(),
      "/oauth2": createProxyConfig(),
      "/login": createProxyConfig(),
      "/error": createProxyConfig(),
    },
  },
});