package frontlinesms2

class SearchControllerIntegrationSpec extends grails.plugin.spock.IntegrationSpec {
	def controller
	def c1, c2
	def g
	def f

	def setup() {
		controller = new SearchController()
		c1 = new Contact(name:'Alex', address:'+254987654').save(failOnError:true)
		c2 = new Contact(name:'Mark', address:'+254333222').save(failOnError:true)
		g = new Group(name:'test').save(failOnError:true)
		
		[new Fmessage(src:'+254987654', dst:'+254987654', text:'work at 11.00'),
			new Fmessage(src:'+254111222', dst:'+254937634', text:'work is awesome'),
			new Fmessage(src:'Bob', dst:'+254987654', text:'hi Bob'),
				new Fmessage(src:'Michael', dst:'+2541234567', text:'Can we get meet in 5 minutes')].each() {
					it.inbound = true
					it.save(failOnError:true)
				}

		def chickenMessage = new Fmessage(src:'Barnabus', dst:'+12345678', text:'i like chicken', inbound:true)
		def liverMessage = new Fmessage(src:'Minime', dst:'+12345678', text:'i like liver', inbound:false)
		def liverMessage2 = new Fmessage(src:'+254333222', dst:'+12345678', text:'liver for lunch?', inbound:false)
		def chickenResponse = new PollResponse(value:'chicken')
		def liverResponse = new PollResponse(value:'liver')
		liverResponse.addToMessages(liverMessage)
		liverResponse.addToMessages(liverMessage2)
		chickenResponse.addToMessages(chickenMessage)
		Poll p = new Poll(title:'Miauow Mix', responses:[chickenResponse, liverResponse]).save(failOnError:true, flush:true)
	}

	def cleanup() {
		Group.findAll()*.delete(flush:true, failOnError:true)
		Contact.findAll()*.delete(flush:true, failOnError:true)
		Poll.findAll()*.delete(flush:true, failOnError:true)
		MessageOwner.findAll()*.delete(flush:true, failOnError:true)
		Fmessage.findAll()*.delete(flush:true, failOnError:true)
	}
	
	def makeGroupMember() {
		c1.addToGroups(g, true)
		assert(Contact.get(c1.id).isMemberOf(Group.get(g.id)))
		c2.addToGroups(g, true)
		assert(Contact.get(c2.id).isMemberOf(Group.get(g.id)))
	}
	
	def "blank search does not return a list of messages"() {
		when:
			controller.params.keywords = ""
			controller.params.groupList = -1
			controller.search()
			def model = controller.modelAndView.model.messageInstanceList
		then:
			!model
	}
	
	def "message searches can be restricted to a poll or folders"() {
		when:
			controller.params.keywords = "chicken"
			controller.params.activityList = Poll.findByTitle('Miauow Mix').id
			controller.params.selectedActivity = 'Miauow Mix'
			controller.params.groupList = -1
			controller.search()
			def model = controller.modelAndView.model.messageInstanceList
		then:
			model == [Fmessage.findBySrc('Barnabus')]
		when:
			new MessageOwner(value: 'work').save(failOnError: true, flush:true)
			f = new Folder(value: 'work')
			f.addToMessages(Fmessage.findBySrc('+254111222')).save(failOnError: true, flush:true)
			controller.params.keywords = "work"
			controller.params.activityList = f.id
			controller.params.selectedActivity = 'work'
			controller.params.groupList = -1
			controller.search()
			def model2 = controller.modelAndView.model.messageInstanceList
		then:
			model2 == [Fmessage.findBySrc('+254111222')]
	}
	
	def "message searches can be restricted to a contact group"() {
		given:
			makeGroupMember()
		when:
			controller.params.keywords = "liver"
			controller.params.groupList = Group.findByName('test').id
			controller.search()
			def model = controller.modelAndView.model.messageInstanceList
		then:
			model == [ Fmessage.findBySrc('+254333222')]
	}
	
	def "groups without contacts do not return messages"() {
		when:
			controller.params.keywords = "test"
			controller.params.groupList = Group.findByName('test').id
			controller.search()
			def model = controller.modelAndView.model.messageInstanceList
		then:
			!model
	}
	
	def "message searches can be restricted to both contact groups and polls"() {
		given:
			makeGroupMember()
		when:
			controller.params.keywords = "liver"
			controller.params.activityList = Poll.findByTitle('Miauow Mix').id
			controller.params.selectedActivity = 'Miauow Mix'
			controller.params.groupList = Group.findByName('test').id
			controller.search()
			def model = controller.modelAndView.model.messageInstanceList
		then:
			model == [Fmessage.findBySrc('+254333222')]
	}
	
	def "deleted messages do not appear in search results"() {
		when:
			controller.params.keywords = "liver"
			controller.params.activityList = Poll.findByTitle('Miauow Mix').id
			controller.params.selectedActivity = 'Miauow Mix'
			Fmessage.findBySrc("+254333222").toDelete().save(flush: true)
			controller.search()
			def model = controller.modelAndView.model.messageInstanceList
		then:
			model == [Fmessage.findBySrc('Minime')]
	}
}