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
    ],
    configureWebpack: {
        optimization: {                     // Fonte: https://webpack.js.org/configuration/optimization/, https://webpack.js.org/plugins/split-chunks-plugin/
            minimize: true,
            splitChunks: {
                minSize: 20000,             // Minimum size, in bytes, for a chunk to be generated.
                maxSize: 60000,             // Try to split chunks bigger than maxSize bytes into smaller parts. Parts will be at least minSize.
            }
        },
        performance: {                      // Fonte: https://webpack.js.org/configuration/performance/
            hints: 'warning',               // Impostare se si vogliono vedere i suggerimenti di performance
            maxEntrypointSize: 500000,      // This option controls when webpack should emit performance hints based on the maximum entry point size in bytes.
            maxAssetSize: 500000            // This option controls when webpack emits a performance hint based on individual asset size in bytes.
        }
    }
}