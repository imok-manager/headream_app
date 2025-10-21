config.resolve = config.resolve || {};
config.resolve.fallback = config.resolve.fallback || {};

// Node.js 모듈 polyfill 설정
config.resolve.fallback = {
    ...config.resolve.fallback,
    "crypto": false,
    "stream": false,
    "http": false,
    "https": false,
    "net": false,
    "tls": false,
    "url": false,
    "bufferutil": false,
    "utf-8-validate": false
};

// 웹 환경에서 불필요한 모듈들 무시
config.externals = config.externals || {};
config.externals = {
    ...config.externals,
    "ws": "{}",
    "bufferutil": "{}",
    "utf-8-validate": "{}"
};
