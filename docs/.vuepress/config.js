/**
 * @see https://vuepress.vuejs.org/zh/
 */
module.exports = {
  port: '4000',
  dest: 'dist',
  base: '/bigdata-tutorial/',
  title: 'BIGDATA-TUTORIAL',
  description: '大数据教程',
  head: [['link', { rel: 'icon', href: `/favicon.ico` }]],
  markdown: {
    externalLinks: {
      target: '_blank',
      rel: 'noopener noreferrer',
    },
  },
  themeConfig: {
    logo: 'https://raw.githubusercontent.com/dunwu/images/dev/common/dunwu-logo-200.png',
    repo: 'dunwu/bigdata-tutorial',
    repoLabel: 'Github',
    docsDir: 'docs',
    docsBranch: 'master',
    editLinks: true,
    smoothScroll: true,
    locales: {
      '/': {
        label: '简体中文',
        selectText: 'Languages',
        editLinkText: '帮助我们改善此页面！',
        lastUpdated: '上次更新',
        nav: [
          {
            text: 'Hive',
            link: '/hive/',
          },
          {
            text: 'Hdfs',
            link: '/hdfs/',
          },
          {
            text: 'Hbase',
            link: '/hbase/',
          },
          {
            text: 'ZooKeeper',
            link: '/zookeeper/',
          },
          {
            text: 'Kafka',
            link: '/kafka/',
          },
          {
            text: '✨ Java系列',
            ariaLabel: 'Java',
            items: [
              {
                text: 'Java 教程 📚',
                link: 'https://dunwu.github.io/java-tutorial/',
                target: '_blank',
                rel: '',
              },
              {
                text: 'JavaCore 教程 📚',
                link: 'https://dunwu.github.io/javacore/',
                target: '_blank',
                rel: '',
              },
              {
                text: 'JavaTech 教程 📚',
                link: 'https://dunwu.github.io/javatech/',
                target: '_blank',
                rel: '',
              },
              {
                text: 'Spring 教程 📚',
                link: 'https://dunwu.github.io/spring-tutorial/',
                target: '_blank',
                rel: '',
              },
              {
                text: 'Spring Boot 教程 📚',
                link: 'https://dunwu.github.io/spring-boot-tutorial/',
                target: '_blank',
                rel: '',
              },
            ],
          },
          {
            text: '🎯 博客',
            link: 'https://github.com/dunwu/blog',
            target: '_blank',
            rel: '',
          },
        ],
        sidebar: 'auto',
        sidebarDepth: 2,
      },
    },
  },
  plugins: [
    [
      '@vuepress/active-header-links',
      {
        sidebarLinkSelector: '.sidebar-link',
        headerAnchorSelector: '.header-anchor',
      },
    ],
    ['@vuepress/back-to-top', true],
    [
      '@vuepress/pwa',
      {
        serviceWorker: true,
        updatePopup: true,
      },
    ],
    [
      '@vuepress/last-updated',
      {
        transformer: (timestamp, lang) => {
          // 不要忘了安装 moment
          const moment = require('moment')
          moment.locale(lang)
          return moment(timestamp).fromNow()
        },
      },
    ],
    ['@vuepress/medium-zoom', true],
    [
      'container',
      {
        type: 'vue',
        before: '<pre class="vue-container"><code>',
        after: '</code></pre>',
      },
    ],
    [
      'container',
      {
        type: 'upgrade',
        before: (info) => `<UpgradePath title="${info}">`,
        after: '</UpgradePath>',
      },
    ],
    ['flowchart'],
  ],
}
