package frontlinesms2.settings

import frontlinesms2.*

class SettingsPage extends geb.Page {
	static url = 'settings'
	static at = {
		title.startsWith('Settings')
	}
	static content = {
		phonesMenuItem(required: false) { $("#settings-menu").children()[0] }
	}
}
