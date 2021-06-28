/**
 * Welcome to your Workbox-powered service worker!
 *
 * You'll need to register this file in your web app and you should
 * disable HTTP caching for this file too.
 * See https://goo.gl/nhQhGp
 *
 * The rest of the code is auto-generated. Please don't update this file
 * directly; instead, make changes to your Workbox build configuration
 * and re-run your build process.
 * See https://goo.gl/2aRDsh
 */

importScripts("https://storage.googleapis.com/workbox-cdn/releases/4.3.1/workbox-sw.js");

self.addEventListener('message', (event) => {
  if (event.data && event.data.type === 'SKIP_WAITING') {
    self.skipWaiting();
  }
});

/**
 * The workboxSW.precacheAndRoute() method efficiently caches and responds to
 * requests for URLs in the manifest.
 * See https://goo.gl/S9QRab
 */
self.__precacheManifest = [
  {
    "url": "404.html",
    "revision": "a7bc5369147b28085caae5c85d7bca79"
  },
  {
    "url": "assets/css/0.styles.8327ee3f.css",
    "revision": "b85fcf74d91ca0b9dca818f5e2deea46"
  },
  {
    "url": "assets/img/search.83621669.svg",
    "revision": "83621669651b9a3d4bf64d1a670ad856"
  },
  {
    "url": "assets/js/10.a80fbf02.js",
    "revision": "a58541ed14879145b698c8059acc5dfb"
  },
  {
    "url": "assets/js/11.7a77b35e.js",
    "revision": "fd2526905b40c075f78b27cbcecbe2a9"
  },
  {
    "url": "assets/js/12.9e3d1666.js",
    "revision": "c6696a1003d4269a8ca79c312272d0f0"
  },
  {
    "url": "assets/js/13.3277fd48.js",
    "revision": "4626657628cc24698a6f58d5576c937a"
  },
  {
    "url": "assets/js/14.bd694374.js",
    "revision": "a12f4faa91a2fb221f2a45b1382895a9"
  },
  {
    "url": "assets/js/15.07805f66.js",
    "revision": "e5e6577c4f8731af4f1d0682519d959d"
  },
  {
    "url": "assets/js/16.75e88870.js",
    "revision": "ac055aceac28f5e0310b53639138c689"
  },
  {
    "url": "assets/js/17.8ced931e.js",
    "revision": "5d0a08211cc1e44f99267822ec8c608f"
  },
  {
    "url": "assets/js/18.a222c567.js",
    "revision": "46fc17abdd501f4ce82e2159bb8aa0ef"
  },
  {
    "url": "assets/js/19.8d5929dc.js",
    "revision": "19c16d31bac8b36a35c231dc9083b1e2"
  },
  {
    "url": "assets/js/20.18d34255.js",
    "revision": "deb572448c7d86a0439554f022f0a7d3"
  },
  {
    "url": "assets/js/21.63c35fd3.js",
    "revision": "cc1019bbfaea4a3f177c8bcb2e5d1876"
  },
  {
    "url": "assets/js/22.dd1674e6.js",
    "revision": "9df2852883112c9d1af39458de21ca42"
  },
  {
    "url": "assets/js/23.7613cc53.js",
    "revision": "8b7167ced716c11771d473e3bb65793f"
  },
  {
    "url": "assets/js/24.903b6146.js",
    "revision": "b0b205a9f026c60238451391c3f96b5e"
  },
  {
    "url": "assets/js/25.179f3524.js",
    "revision": "c6f0477f320c2829f8286b7dff3f1364"
  },
  {
    "url": "assets/js/26.25fc570d.js",
    "revision": "e934809065a3daa6b958f405ac376e11"
  },
  {
    "url": "assets/js/27.d983a320.js",
    "revision": "45e48d704fe2a4291d2b43567ad284c4"
  },
  {
    "url": "assets/js/28.af982ea3.js",
    "revision": "bd7d01db1847dc6a20664716f29cdef6"
  },
  {
    "url": "assets/js/29.d1bd2fc9.js",
    "revision": "ff60b4acbf715316c74accafcf128982"
  },
  {
    "url": "assets/js/30.b2e9d0d9.js",
    "revision": "69955c3cca7d1c35a5543e081e41d877"
  },
  {
    "url": "assets/js/31.0b49e0a2.js",
    "revision": "137231f2784806d30fca79d5a251700b"
  },
  {
    "url": "assets/js/32.cb25c6d7.js",
    "revision": "0478d917c3fbdd0526dcdd56784599ca"
  },
  {
    "url": "assets/js/33.8e5e53c5.js",
    "revision": "6c0cb37785631978678fc3386a9f0854"
  },
  {
    "url": "assets/js/34.997b11b0.js",
    "revision": "2440aed502a9da20b72b178f885a9aa8"
  },
  {
    "url": "assets/js/35.ed0026eb.js",
    "revision": "39ff088b96db737f289ac53581438002"
  },
  {
    "url": "assets/js/36.ef02c891.js",
    "revision": "5c084a5fd059876900ddfd8131affccc"
  },
  {
    "url": "assets/js/37.31f8fc60.js",
    "revision": "653e7fa8f23ae044c7fc1bcb67ede2db"
  },
  {
    "url": "assets/js/38.59d9a10d.js",
    "revision": "da97f887c82942ff625d8cb74c53f228"
  },
  {
    "url": "assets/js/39.0136f94f.js",
    "revision": "0da618f577a8cdbd45ba841c8bd3c223"
  },
  {
    "url": "assets/js/4.f8347e07.js",
    "revision": "76954a78e5d6821b7f74866174b48921"
  },
  {
    "url": "assets/js/40.00bd91c6.js",
    "revision": "318550da7cfb67e7fb6bc2e7d716ae03"
  },
  {
    "url": "assets/js/41.eaa1e829.js",
    "revision": "f12828fe003cf1965f576209f97da12d"
  },
  {
    "url": "assets/js/42.27ac9c20.js",
    "revision": "054edbc660e179cb8ba40d4684d37dca"
  },
  {
    "url": "assets/js/43.bc275700.js",
    "revision": "67fd6927abbba347fabe219dd84645f8"
  },
  {
    "url": "assets/js/44.8de943ab.js",
    "revision": "1e243b2ba468c4b8716a081c6b9caa3a"
  },
  {
    "url": "assets/js/45.31f7910c.js",
    "revision": "cd6618b9465c3d4ef10f1cffb5212edf"
  },
  {
    "url": "assets/js/46.b0b5a469.js",
    "revision": "29bdb55f979a6cf25e391bfb342c07a5"
  },
  {
    "url": "assets/js/47.a0e3109d.js",
    "revision": "f5801b62828395305d4432aeed443745"
  },
  {
    "url": "assets/js/48.dc562b81.js",
    "revision": "f4c98d8ecb903bb3055d2bffc2043046"
  },
  {
    "url": "assets/js/49.67d96b78.js",
    "revision": "a9e568bc2a9d0e68e012e3dd26bebf5b"
  },
  {
    "url": "assets/js/5.f1c043bd.js",
    "revision": "5660892119d6fbef761f2d199ab22eb4"
  },
  {
    "url": "assets/js/50.83ef049e.js",
    "revision": "aadecb1610eb45f855447927ffb7728c"
  },
  {
    "url": "assets/js/6.dc654452.js",
    "revision": "c6af3a726ba9639fea253ecd0224daba"
  },
  {
    "url": "assets/js/7.d18d03ab.js",
    "revision": "976f0ecbb45b872a4dfd061d5e2d1fe8"
  },
  {
    "url": "assets/js/8.a5f38894.js",
    "revision": "a66915005b8da2f0a9d91ff522c33f3e"
  },
  {
    "url": "assets/js/9.69b11c1a.js",
    "revision": "92d6f9e6317ea3d4f5822361135fee6f"
  },
  {
    "url": "assets/js/app.04a9c37e.js",
    "revision": "91ae36f592508bb6621d2ae7c5f49cb7"
  },
  {
    "url": "assets/js/vendors~flowchart.4af2219b.js",
    "revision": "79ff26e6a72976119d3ca6ae87e3dafd"
  },
  {
    "url": "assets/js/vendors~notification.720c539b.js",
    "revision": "e0cbc3a164d5b5d0732607021147b312"
  },
  {
    "url": "bigdata-study.html",
    "revision": "01611d847692569a1d819bbcfbd02e57"
  },
  {
    "url": "flink/flink.html",
    "revision": "4a038e49c0dc5ebd660ccb622b23e552"
  },
  {
    "url": "flume.html",
    "revision": "7060b90571524d88e92775d705cb9ff3"
  },
  {
    "url": "hbase/hbase-api.html",
    "revision": "2a37817746f1494da2e5cd9321277e41"
  },
  {
    "url": "hbase/hbase-cli.html",
    "revision": "a97d8a6289f50ba38e43ed59bb709f70"
  },
  {
    "url": "hbase/hbase-ops.html",
    "revision": "84aefb775c0428a495e759e163021754"
  },
  {
    "url": "hbase/hbase-quickstart.html",
    "revision": "82e7aa6cc64f39105c7a0495992bc82f"
  },
  {
    "url": "hbase/index.html",
    "revision": "22b8fbbf019123462f7d715570806496"
  },
  {
    "url": "hdfs/hdfs-java-api.html",
    "revision": "65a6ebb201b98f761fb6100c26a3268c"
  },
  {
    "url": "hdfs/hdfs-ops.html",
    "revision": "fa10023308a95795df1dc342a1df331b"
  },
  {
    "url": "hdfs/hdfs-quickstart.html",
    "revision": "b34547b8b820cf95fbc0e61515cc09fb"
  },
  {
    "url": "hdfs/index.html",
    "revision": "166b3460951a991367a4fd773c573040"
  },
  {
    "url": "hive/hive-ddl.html",
    "revision": "f30fbf08470c73061c75cec6130f576d"
  },
  {
    "url": "hive/hive-dml.html",
    "revision": "b798df64e81b505308e77e64eb9ac1c8"
  },
  {
    "url": "hive/hive-index-and-view.html",
    "revision": "96d0b18cf39d8a4e32fb4a901db34bbe"
  },
  {
    "url": "hive/hive-ops.html",
    "revision": "2b2b01f1156517067e784dd50c030ff5"
  },
  {
    "url": "hive/hive-query.html",
    "revision": "510e80392c3c4cafca6d1564577a5e4b"
  },
  {
    "url": "hive/hive-quickstart.html",
    "revision": "c739ded9adc19179410e6d5073716d66"
  },
  {
    "url": "hive/hive-table.html",
    "revision": "5e6950b42f947dc5e86bef2fd8d5e88f"
  },
  {
    "url": "hive/index.html",
    "revision": "cb8268ceafae1c0e22260252d982fcd1"
  },
  {
    "url": "images/dunwu-logo-100.png",
    "revision": "724d2445b33014d7c1ad9401d24a7571"
  },
  {
    "url": "images/dunwu-logo-200.png",
    "revision": "0a7effb33a04226ed0b9b6e68cbf694d"
  },
  {
    "url": "images/dunwu-logo-50.png",
    "revision": "9ada5bdcd34ac9c06dcd682b70a9016b"
  },
  {
    "url": "images/dunwu-logo.png",
    "revision": "f85f8cd2ab66992bc87b0bb314fdcf59"
  },
  {
    "url": "index.html",
    "revision": "298675f9721750d9c8e9ca382aa26e7c"
  },
  {
    "url": "kafka/index.html",
    "revision": "7210970e3a6c0a5ed98c3b39e554b449"
  },
  {
    "url": "kafka/Kafka可靠传输.html",
    "revision": "994c00cb9a9520352189a80d678bf8cb"
  },
  {
    "url": "kafka/Kafka存储.html",
    "revision": "03ce68cc37af376e03ddf1db49914ca1"
  },
  {
    "url": "kafka/Kafka快速入门.html",
    "revision": "0891d12871555de8cb312bc0474a2cf0"
  },
  {
    "url": "kafka/Kafka流式处理.html",
    "revision": "4492c08c26e242fe15fb511302046930"
  },
  {
    "url": "kafka/Kafka消费者.html",
    "revision": "4a34916ac1b2ddc5837c6cf1d9fc02a3"
  },
  {
    "url": "kafka/Kafka生产者.html",
    "revision": "bd4caca004a90e4f14328187b6880db0"
  },
  {
    "url": "kafka/Kafka运维.html",
    "revision": "75e2cfb230d569fc1c8429a2e0157d87"
  },
  {
    "url": "kafka/Kafka集群.html",
    "revision": "56e2088d4fec7327f6a4182dade8ef8e"
  },
  {
    "url": "mapreduce/mapreduce.html",
    "revision": "9d5d10948eb6d0f944c816cc9c5710ef"
  },
  {
    "url": "overview.html",
    "revision": "e7a3ba76898d079cda6aa897378f6b25"
  },
  {
    "url": "scala.html",
    "revision": "7fb71851414d1023378acdb471cc1787"
  },
  {
    "url": "spark.html",
    "revision": "e75602ec4020b8598c1c948a2e6e00f5"
  },
  {
    "url": "sqoop.html",
    "revision": "e0a7d9faf86fc40e1f2d3ee137a2dee5"
  },
  {
    "url": "yarn.html",
    "revision": "42492f379fbee794c99a5b4692cfef7c"
  },
  {
    "url": "zookeeper/index.html",
    "revision": "24e35de057d829933155059698a62a69"
  },
  {
    "url": "zookeeper/zookeeper-api.html",
    "revision": "e419268e3c6eb6b0bdbdc8f02a96ff18"
  },
  {
    "url": "zookeeper/zookeeper-ops.html",
    "revision": "1ca04e6870c677c4eb574aa199b90708"
  },
  {
    "url": "zookeeper/zookeeper-quickstart.html",
    "revision": "d2bb89f43ae416da06dd1435c03e5a79"
  }
].concat(self.__precacheManifest || []);
workbox.precaching.precacheAndRoute(self.__precacheManifest, {});
addEventListener('message', event => {
  const replyPort = event.ports[0]
  const message = event.data
  if (replyPort && message && message.type === 'skip-waiting') {
    event.waitUntil(
      self.skipWaiting().then(
        () => replyPort.postMessage({ error: null }),
        error => replyPort.postMessage({ error })
      )
    )
  }
})
