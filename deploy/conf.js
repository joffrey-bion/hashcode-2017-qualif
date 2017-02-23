let profile = '';

exports.config = {
  seleniumAddress: 'http://localhost:4444/wd/hub',
  specs: ['test.js'],
  capabilities: {
    'browserName': 'chrome',
    'chromeOptions': {
      'args': ['user-data-dir=C:/Users/lpontoise/AppData/Local/Google/Chrome/Protractor']
    }
  }
};