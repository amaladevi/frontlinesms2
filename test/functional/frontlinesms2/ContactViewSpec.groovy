package frontlinesms2

import geb.Browser
import org.openqa.selenium.firefox.FirefoxDriver
import grails.plugin.geb.GebSpec

class ContactViewSpec extends grails.plugin.geb.GebSpec {
	def 'contacts list is displayed'() {
		when:
			go 'contact'
			println $('body').text()
		then:
			def contactList = $('#contacts')
			assert contactList.tag() == 'ol'
			
			def contactNames = contactList.children().collect() {
				it.text()
			}
			assert contactNames == ['Alice', 'Bob']
	}
    
	def 'contacts list not shown when no contacts exist'() {
		when:
			println 'Deleting contacts...'
			// TODO should not need to delete all contacts manually
			def allContacts = frontlinesms2.Contact.findAll()
			println "All contacts: ${allContacts}"
			allContacts.each() { 
				println "Deleting ${it.name}"
				it.delete(failOnError: true, flush: true)
				
			}
			println 'deleted.'
			go 'http://localhost:8080/frontlinesms2/contact'
		then:
			def c = $('#contacts')
			println "contacts content after deletion: ${c.text()}"
			assert c.tag() == "div"
			assert c.text() == 'You have no contacts saved'
	}
}