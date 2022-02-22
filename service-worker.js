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
    "revision": "4aa19db81cfaec3eab67bc163c11d7f0"
  },
  {
    "url": "assets/css/0.styles.eecf7a64.css",
    "revision": "c890d2c244355ec4e31ad714c5b112ff"
  },
  {
    "url": "assets/img/search.83621669.svg",
    "revision": "83621669651b9a3d4bf64d1a670ad856"
  },
  {
    "url": "assets/js/10.1ab7bd74.js",
    "revision": "ce19a6ed1e4e43b74791456efed3e5b1"
  },
  {
    "url": "assets/js/11.a42f7604.js",
    "revision": "081aac1ce0f9563ef424716f7ae15de6"
  },
  {
    "url": "assets/js/12.7dce2ed8.js",
    "revision": "26fdc917e55bc7b0a0514a93fb4bc343"
  },
  {
    "url": "assets/js/13.1b26207d.js",
    "revision": "667efc56b60b37584e5d61d5724e2b1d"
  },
  {
    "url": "assets/js/14.a9835374.js",
    "revision": "fb33491246dff70096157b070e0eb13a"
  },
  {
    "url": "assets/js/15.90c66596.js",
    "revision": "509b6cb1f3b14b0b583edf7a9d58bd25"
  },
  {
    "url": "assets/js/16.e0471453.js",
    "revision": "02f4c1ff537b3fd8a619a9b0d7757e89"
  },
  {
    "url": "assets/js/17.2c0565cb.js",
    "revision": "4af061be02801e90ab78e0cc3f0c4506"
  },
  {
    "url": "assets/js/18.fabbce00.js",
    "revision": "16f345da93774f1d6cdd7fe15c24ffca"
  },
  {
    "url": "assets/js/19.c120a108.js",
    "revision": "d3af47d4e3980a154d217fd0329734d5"
  },
  {
    "url": "assets/js/20.5a4f48af.js",
    "revision": "b47e3f3d021ec918999633a4d93beb58"
  },
  {
    "url": "assets/js/21.d3fce730.js",
    "revision": "b1955085a5bdb2d922a112e2a28bf822"
  },
  {
    "url": "assets/js/22.00ac574a.js",
    "revision": "93ed17ff235c8f6792615ebd636b1b22"
  },
  {
    "url": "assets/js/23.754595c9.js",
    "revision": "e6a6e121b1e5962e152da8fb49b588d7"
  },
  {
    "url": "assets/js/24.fc9d31a1.js",
    "revision": "78f46c9b6f915dd5fd0795dc7db6ed22"
  },
  {
    "url": "assets/js/25.d4313bcb.js",
    "revision": "3e7cf726ce3a3c761ab7eda15e7ac802"
  },
  {
    "url": "assets/js/26.604f045b.js",
    "revision": "38c831616144df8515ea396fcaf14ff5"
  },
  {
    "url": "assets/js/27.22a239b2.js",
    "revision": "994734465ac325aa191b93cede3f4a00"
  },
  {
    "url": "assets/js/28.466b9a71.js",
    "revision": "860b5e36dac98de779d6add442899902"
  },
  {
    "url": "assets/js/29.2783ebfd.js",
    "revision": "1e835705452b11d71fa142e6ebe9ee15"
  },
  {
    "url": "assets/js/30.2ff3250e.js",
    "revision": "e9241b0d6103eb47366bc15d3b85c322"
  },
  {
    "url": "assets/js/31.2c64e404.js",
    "revision": "cfe77b5cf0d12e6702c24f8e6e517bc5"
  },
  {
    "url": "assets/js/32.0ab8b31c.js",
    "revision": "4a6f49247a9a06799d857259456964b2"
  },
  {
    "url": "assets/js/33.be38d2dc.js",
    "revision": "133d5b7c79feacda19f4f842bf1bd789"
  },
  {
    "url": "assets/js/34.811f6626.js",
    "revision": "dbafdf8d8c4b458d630a8723061ae077"
  },
  {
    "url": "assets/js/35.9d9f3847.js",
    "revision": "7d3779f00f4704632dd652ec6b699dfc"
  },
  {
    "url": "assets/js/36.32bb3f84.js",
    "revision": "302d1966efd9468aeeb1aa7f7b22e2d3"
  },
  {
    "url": "assets/js/37.db182f29.js",
    "revision": "948faf8cab3298f570f9c37cb11247af"
  },
  {
    "url": "assets/js/38.91a9d6f3.js",
    "revision": "e723b7afaeee7ced9e3354fd2a8cb1a8"
  },
  {
    "url": "assets/js/39.1148f971.js",
    "revision": "c605584e239d066e89f81cd1dc1166ea"
  },
  {
    "url": "assets/js/4.cda5e0f1.js",
    "revision": "a0476d45222e65b90f7c73c090fe2bb4"
  },
  {
    "url": "assets/js/40.1536c75c.js",
    "revision": "c9254160c84365171606d35f6cf86c7f"
  },
  {
    "url": "assets/js/41.31725066.js",
    "revision": "1e997598f43f78c6beb49e9deb69dc0c"
  },
  {
    "url": "assets/js/42.68e59bdd.js",
    "revision": "dd429af712d7bc7ea5b398a989da847d"
  },
  {
    "url": "assets/js/43.9bac72d5.js",
    "revision": "eac1b7ce4e289d81f8bcab36cf073135"
  },
  {
    "url": "assets/js/44.a2edd3e0.js",
    "revision": "9775d3fe7e569deb2f7b9f521c6451a6"
  },
  {
    "url": "assets/js/45.4a1cf2ae.js",
    "revision": "15feae1af95d3d8ead7595c3cd376fb1"
  },
  {
    "url": "assets/js/46.d8a80a79.js",
    "revision": "710aedc7747dec196a4b5f9a10a46b0f"
  },
  {
    "url": "assets/js/47.9621b1a5.js",
    "revision": "263076df9ab57098a03febef7e6b7cb4"
  },
  {
    "url": "assets/js/48.56d4fa14.js",
    "revision": "173eb318cf42566cae39a63e581239ce"
  },
  {
    "url": "assets/js/49.d9d143dd.js",
    "revision": "7bc039d49f5981f445d63beac7bdfa48"
  },
  {
    "url": "assets/js/5.9d6e4fe2.js",
    "revision": "a2bc58c7347f5350e17d35436662bf62"
  },
  {
    "url": "assets/js/50.6e4aa6cd.js",
    "revision": "83b9a02bcc2993f6d450620cfb9d1f2b"
  },
  {
    "url": "assets/js/51.55771fda.js",
    "revision": "7de5d4e0ab95daaa54f036d55c588f3a"
  },
  {
    "url": "assets/js/52.ee51188b.js",
    "revision": "987920335c01e8b40bca85947cd30d76"
  },
  {
    "url": "assets/js/53.b014e486.js",
    "revision": "7e2f09844790a75bda66eb6884b25586"
  },
  {
    "url": "assets/js/54.ae66c176.js",
    "revision": "676dc98fda89f5b738c5e8e6672b5b09"
  },
  {
    "url": "assets/js/55.2ea9bfd9.js",
    "revision": "2dd13a7e50a9f75ae2c0dc14884e8cf7"
  },
  {
    "url": "assets/js/56.ffb4a04b.js",
    "revision": "34ea2bf7a6f802471028d74afeb961b8"
  },
  {
    "url": "assets/js/57.bdee2f7e.js",
    "revision": "08a860431b574c56acaec793b49177a9"
  },
  {
    "url": "assets/js/58.982c2219.js",
    "revision": "0d255dc9bc93af3b123a0884b0165328"
  },
  {
    "url": "assets/js/59.c006237e.js",
    "revision": "2d48cda06df26bedfc74e9c3bb05c71c"
  },
  {
    "url": "assets/js/6.3bb0b816.js",
    "revision": "b60c4a5a073e538307e76a549a08056f"
  },
  {
    "url": "assets/js/60.b27dc3c8.js",
    "revision": "0b66be5693ffa66c3f446c4458827dba"
  },
  {
    "url": "assets/js/7.d55602d2.js",
    "revision": "e9881f71adea6dd77359b8310af905f0"
  },
  {
    "url": "assets/js/8.89e9ace9.js",
    "revision": "e843420f3219dd959c2e35878c8ff0e0"
  },
  {
    "url": "assets/js/9.0c40eb95.js",
    "revision": "edc7a51fa4467e0ad5ddb6799c64c23e"
  },
  {
    "url": "assets/js/app.3ada26ba.js",
    "revision": "52b2e4c17c575c0ba50aaa4be2801ae2"
  },
  {
    "url": "assets/js/vendors~flowchart.5998caa6.js",
    "revision": "c10f81949701fa78de115e736aff2d77"
  },
  {
    "url": "assets/js/vendors~notification.943e54fe.js",
    "revision": "846de6e5c6f3387c4bbbb661c9b5ddf1"
  },
  {
    "url": "bigdata-study.html",
    "revision": "6647d6da676fd32d2b10770e37e5c04c"
  },
  {
    "url": "flink/flink.html",
    "revision": "d8f4f983147a8135f1305beff435dacd"
  },
  {
    "url": "flink/FlinkApi.html",
    "revision": "01096cfbe4788aafba76e926ae2bb113"
  },
  {
    "url": "flink/FlinkETL.html",
    "revision": "bbe0390123f4f5a6f5a0ddfa23b333c4"
  },
  {
    "url": "flink/Flink事件驱动.html",
    "revision": "5efae0fc8d1d6084ad86b179feb2f31d"
  },
  {
    "url": "flink/Flink实时流处理.html",
    "revision": "bde7cdbe31a73612c13922b5b8524735"
  },
  {
    "url": "flink/Flink有状态流处理.html",
    "revision": "773ce291a8817c026e20acb56f3e5164"
  },
  {
    "url": "flink/Flink架构.html",
    "revision": "3a6e2bd1f5dd74614b63f1d28aef1fea"
  },
  {
    "url": "flink/Flink简介.html",
    "revision": "a9e9e46636d2ff83e75d8ec4996d6aa7"
  },
  {
    "url": "flink/index.html",
    "revision": "50868d9692536460f1b8ef9b9ede921b"
  },
  {
    "url": "flume.html",
    "revision": "34e13f5a085b336fca1c00de189994b6"
  },
  {
    "url": "hbase/HBase原理.html",
    "revision": "8f56c02cebf3153115c43b859ed38e23"
  },
  {
    "url": "hbase/HBase命令.html",
    "revision": "7182eeea27ca9ca884cbd20d3ab44992"
  },
  {
    "url": "hbase/HBase应用.html",
    "revision": "d81a3bbd1fcf4cc113bba161ed9364f5"
  },
  {
    "url": "hbase/HBase运维.html",
    "revision": "c965e95b6c1652fb6d843cb7fd2e3654"
  },
  {
    "url": "hbase/index.html",
    "revision": "0eea689d0addcd8d397d3913c32d0e71"
  },
  {
    "url": "hdfs/hdfs-java-api.html",
    "revision": "b00d922a931c81a6baaf43449fe29722"
  },
  {
    "url": "hdfs/hdfs-ops.html",
    "revision": "750c4535d9cbbc178b5c6a9348df60e0"
  },
  {
    "url": "hdfs/hdfs-quickstart.html",
    "revision": "7f3ce97d4e8d1c199fbdc13b95438556"
  },
  {
    "url": "hdfs/index.html",
    "revision": "71daaf35a0bf334a2404aba83b29fa44"
  },
  {
    "url": "hive/hive-ddl.html",
    "revision": "802e055b8c31523ddb3b014a9773564e"
  },
  {
    "url": "hive/hive-dml.html",
    "revision": "1091149c63535a10e4abc6b16815d9b1"
  },
  {
    "url": "hive/hive-index-and-view.html",
    "revision": "4f3fce5728db47ae2bf9c5d46ff71c67"
  },
  {
    "url": "hive/hive-ops.html",
    "revision": "3bbd771d31a2def3c279e2b3c08f56e3"
  },
  {
    "url": "hive/hive-query.html",
    "revision": "581cea1eb9aa5d13186696729a52400b"
  },
  {
    "url": "hive/hive-quickstart.html",
    "revision": "bbf31c1f6f8c6bb9c3a9411a9100fd53"
  },
  {
    "url": "hive/hive-table.html",
    "revision": "b38529181cf18dd79095a007f7825bdc"
  },
  {
    "url": "hive/index.html",
    "revision": "998a9b72aebe30a8e2fdba8b8db2565f"
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
    "revision": "54bd8ffd0d9c44d3f84fdaa269fe1373"
  },
  {
    "url": "kafka/index.html",
    "revision": "91dceb3e1d5e95faaacc062867fdfbf4"
  },
  {
    "url": "kafka/Kafka可靠传输.html",
    "revision": "dfa5dbc0c1b6a26aeb2c07e85b83512b"
  },
  {
    "url": "kafka/Kafka存储.html",
    "revision": "d524dbb3d219dc1d6f5995915eb991a4"
  },
  {
    "url": "kafka/Kafka快速入门.html",
    "revision": "285c05c7567394ae7c5554eb60afffc5"
  },
  {
    "url": "kafka/Kafka流式处理.html",
    "revision": "75b197702e171d7c0675ea77faa9739d"
  },
  {
    "url": "kafka/Kafka消费者.html",
    "revision": "5f29763ff4c3295fe5f2953d1be5baa4"
  },
  {
    "url": "kafka/Kafka生产者.html",
    "revision": "9f7f81d44da160bd08af2626b320f422"
  },
  {
    "url": "kafka/Kafka运维.html",
    "revision": "964c9ae2f2990a9124bc3da9dec37a57"
  },
  {
    "url": "kafka/Kafka集群.html",
    "revision": "0cbeb5e987b7a66b079b65dfefcebd10"
  },
  {
    "url": "mapreduce/mapreduce.html",
    "revision": "253b9ddc9fb417ab84a2d5cc25fbb813"
  },
  {
    "url": "overview.html",
    "revision": "a055e1fec4c6af85b1df9174c51b50e3"
  },
  {
    "url": "scala.html",
    "revision": "85caae04c23e34c9a08f589ab866cc46"
  },
  {
    "url": "spark.html",
    "revision": "e251290304ebf5607c0d224477d76fca"
  },
  {
    "url": "sqoop.html",
    "revision": "c60bd07bdf85d78c8b7bee41829038e5"
  },
  {
    "url": "yarn.html",
    "revision": "18d8306caa63fcac038f8cfbc82fca64"
  },
  {
    "url": "zookeeper/index.html",
    "revision": "d1881de132b45ba2f90d8569ca7deb8b"
  },
  {
    "url": "zookeeper/ZooKeeperAcl.html",
    "revision": "2f0fb68a2238f7d1e5a56e97b879eb13"
  },
  {
    "url": "zookeeper/ZooKeeperJavaApi.html",
    "revision": "999b27e087a4568da4769fa5d4871325"
  },
  {
    "url": "zookeeper/ZooKeeper原理.html",
    "revision": "fbb1ce84b0fda6120cd9283f5d20ea4a"
  },
  {
    "url": "zookeeper/ZooKeeper命令.html",
    "revision": "d55edbd04da1b067279e87f9c8a8b44e"
  },
  {
    "url": "zookeeper/ZooKeeper运维.html",
    "revision": "e0d1930669fe53add2b5d78911977b3f"
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
