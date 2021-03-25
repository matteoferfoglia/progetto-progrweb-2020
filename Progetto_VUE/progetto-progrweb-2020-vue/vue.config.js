// vue.config.js

/* vue.config.js is an optional config file that will be automatically
 * loaded by @vue/cli-service if it's present in your project root (next
 * to package.json). You can also use the vue field in package.json, but
 * do note in that case you will be limited to JSON-compatible values only.
 *
 * Fonte: https://cli.vuejs.org/config/#global-cli-config */

module.exports = {
    productionSourceMap: false,  // evita source maps in production
    transpileDependencies: [     // transpiling delle dipendenze se hanno delle funzionalità non supportate nei browser in cui l'app dovrebbe funzionare
        "axios",
        "core-js",
        "vue",
        "vue-router"
    ]
}