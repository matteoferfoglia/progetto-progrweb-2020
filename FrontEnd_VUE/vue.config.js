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
    // chainWebpack: config => {
    //     config.optimization.minimize(true), // flag: true se si vuole minificare i file in production mode (se non specificato, default: true)
    //     config.performance                  // Modificare le dimensioni dei file per cui mostrare warning durante la compilazione, Fonte: https://forum.vuejs.org/t/asset-size-limit-warning/40429/4
    //         .maxEntrypointSize(500000)
    //         .maxAssetSize(500000)
    // },
    configureWebpack: {
        optimization: {             // Specifica come suddividere (in base alle dimensioni) i file JS prodotti, Fonte: https://stackoverflow.com/a/52634444
            splitChunks: {
                minSize: 10000,
                maxSize: 250000,
            }
        }
    }
}