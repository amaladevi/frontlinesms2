package frontlinesms2.connection

import frontlinesms2.*

abstract class ConnectionGebSpec extends grails.plugin.geb.GebSpec {

	static createTestConnection() {
		[new EmailFconnection(name:'test email connection', protocol:EmailProtocol.IMAPS, serverName:'imap.zoho.com',
				serverPort:993, username:'mr.testy@zoho.com', password:'mter')].each() { it.save(failOnError:true) }
	}
	
	static createTestSMSConnection() {
		[new SmslibFconnection(name:'MTN Dongle', port:'stormyPort')].each() { it.save(failOnError:true) }
	}
	
	static deleteTestConnections() {
		Fconnection.findAll().each() {
			it.refresh()
			it.delete(failOnError:true, flush:true)
		}
	}
}
