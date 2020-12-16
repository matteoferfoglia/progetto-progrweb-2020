module.exports = {
  preset: '@vue/cli-plugin-unit-jest',
  "testMatch": [
    "**/src/test/*.js?(x)"
  ],
  transform: {
    '^.+\\.vue$': 'vue-jest'
  }
}
