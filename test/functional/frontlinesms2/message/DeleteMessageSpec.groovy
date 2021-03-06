package frontlinesms2.message

import frontlinesms2.*

class DeleteMessageSpec extends grails.plugin.geb.GebSpec {
		
	def 'deleted messages do not show up in inbox or poll or folder views'() {
		given:
			createTestData()
			assert Fmessage.getInboxMessages().size() == 3
			assert Poll.findByTitle('Miauow Mix').messages.size() == 2
			assert Fmessage.getFolderMessages(Folder.findByValue('Fools').id).size() == 2
		when:
			go "message/inbox/show/${Fmessage.findBySrc('Bob').id}"
			def btnDelete = $('#message-details .buttons a')
			btnDelete.click()
            waitFor { $("div.flash.message").text().contains("Fmessage") }
		then:
			Fmessage.getInboxMessages().size() == 2

		when:
			go "message/poll/${Poll.findByTitle('Miauow Mix').id}/show/${Fmessage.findBySrc('Barnabus').id}"
			def btnDeleteFromPoll = $('#message-details .buttons a')
			btnDeleteFromPoll.click()
            waitFor { $("div.flash.message").text().contains("Fmessage") }
		then:
			Poll.findByTitle('Miauow Mix').messages.size() == 1

		when:
			go "message/folder/${Folder.findByValue('Fools').id}/show/${Fmessage.findBySrc('Cheney').id}"
			def btnDeleteFromFolder = $('#message-details .buttons a')
			btnDeleteFromFolder.click()
            waitFor { $("div.flash.message").text().contains("Fmessage") }
		then:
			Fmessage.getFolderMessages(Folder.findByValue('Fools').id).size() == 1
		cleanup:
			deleteTestData()
	}
	
	def 'delete button appears in message show view and works'() {
		given:
			createTestData()
			def bob = Fmessage.findBySrc("Bob")
		when:
			go "message/inbox/show/${bob.id}"
			def btnDelete = $('#message-details .buttons a')
		then:
			btnDelete
		when:
			btnDelete.click()
		then:
			at MessagesPage
		when:
			bob.refresh()
		then:
			bob.deleted
		cleanup:
			deleteTestData()
	}
	
	static createTestData() {
		[new Fmessage(src:'Bob', dst:'+254987654', text:'hi Bob'),
				new Fmessage(src:'Alice', dst:'+2541234567', text:'hi Alice'),
				new Fmessage(src:'+254778899', dst:'+254112233', text:'test')].each() {
					it.inbound = true
					it.save(failOnError:true)
				}
		[new Fmessage(src:'Mary', dst:'+254112233', text:'hi Mary'),
				new Fmessage(src:'+254445566', dst:'+254112233', text:'test')].each() {
					it.inbound = false
					it.save(failOnError:true)
				}

		def chickenMessage = new Fmessage(src:'Barnabus', dst:'+12345678', text:'i like chicken', inbound:true)
		def liverMessage = new Fmessage(src:'Minime', dst:'+12345678', text:'i like liver', inbound:false)
		def chickenResponse = new PollResponse(value:'chicken')
		def liverResponse = new PollResponse(value:'liver')
		liverResponse.addToMessages(liverMessage)
		chickenResponse.addToMessages(chickenMessage)
		new Poll(title:'Miauow Mix', responses:[chickenResponse, liverResponse]).save(failOnError:true, flush:true)

		def message1 = new Fmessage(src:'Cheney', dst:'+12345678', text:'i hate chicken', inbound:false)
		def message2 = new Fmessage(src:'Bush', dst:'+12345678', text:'i hate liver', inbound:false)
		def fools = new Folder(value:'Fools').save(failOnError:true, flush:true)
		fools.addToMessages(message1)
		fools.addToMessages(message2)
		fools.save(failOnError:true, flush:true)
	}
	
	static deleteTestData() {

		Poll.findAll().each() {
			it.refresh()
			it.delete(failOnError:true, flush:true)
		}

		Folder.findAll().each() {
			it.refresh()
			it.delete(failOnError:true, flush:true)
		}

		Fmessage.findAll().each() {
			it.refresh()
			it.delete(failOnError:true, flush:true)
		}
	}
}

