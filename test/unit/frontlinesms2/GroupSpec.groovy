package frontlinesms2

import spock.lang.*
import grails.plugin.spock.*

class GroupSpec extends UnitSpec {
	def "group may have a name"() {
		when:
			Group g = new Group()
			assert g.name == null
			g.name = 'People'
		then:
			g.name == 'People'
	}

	def "group must have a name"() {
		when:
			def noNameGroup = new Group()
			def namedGroup = new Group(name:'People')
			mockForConstraintsTests(Group, [noNameGroup, namedGroup])
		then:
			!noNameGroup.validate()
			namedGroup.validate()
	}

	def "group must have unique name"() {
		when:
			def name1Group = new Group(name:'Same')
			def name2Group = new Group(name:'Same')
			mockForConstraintsTests(Group, [name1Group, name2Group])
		then:
			!name1Group.validate()
			!name2Group.validate()
	}

	def "group name must be less than 255 characters"() {
		when:
			def longNameGroup = new Group(name:'0123456789' +
												'0123456789' +
												'0123456789' +
												'0123456789' +
												'0123456789' +
												'0123456789' +
												'0123456789' +
												'0123456789' +
												'0123456789' +
												'0123456789' +
												'0123456789' +
												'0123456789' +
												'0123456789' +
												'0123456789' +
												'0123456789' +
												'0123456789' +
												'0123456789' +
												'0123456789' +
												'0123456789' +
												'0123456789' +
												'0123456789' +
												'0123456789' +
												'0123456789' +
												'0123456789' +
												'0123456789' +
												'0123456789')
			mockForConstraintsTests(Group, [longNameGroup])
		then:
			assert longNameGroup.name.length() > 255
			!longNameGroup.validate()
	}
}

