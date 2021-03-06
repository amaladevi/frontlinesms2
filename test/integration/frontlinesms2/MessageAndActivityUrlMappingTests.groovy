package frontlinesms2

class MessageAndActivityUrlMappingTests extends grails.test.GrailsUrlMappingsTestCase {
	def testInboxView() {
		assertForwardUrlMapping('/message/inbox/show/123', controller:'message', action:'inbox') {
			messageId = 123
		}
	}
	
	def testPollMessageView() {
		assertForwardUrlMapping('/message/poll/123/show/456', controller:'message', action:'poll') {
			messageId = 456
			ownerId = 123
		}
	}
	
	def testFolderMessageView() {
		assertForwardUrlMapping('/message/folder/123/show/456', controller:'message', action:'folder') {
			messageId = 456
			ownerId = 123
		}
	}
}

