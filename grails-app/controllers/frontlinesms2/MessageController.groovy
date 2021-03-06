package frontlinesms2

class MessageController {
	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def index = {
		redirect(action:'inbox')
	}

   def show = { messageInstanceList ->
        def messageInstance = params.messageId ? Fmessage.get(params.messageId) : messageInstanceList[0]
        messageInstance?.updateDisplaySrc()
        if (messageInstance && !messageInstance.read) {
          messageInstance.read = true
          messageInstance.save()
        }
         [messageInstance: messageInstance,
                folderInstanceList: Folder.findAll(),
                pollInstanceList: Poll.findAll()]
   }


  def inbox = {
		def messageInstanceList = Fmessage.getInboxMessages()
    	messageInstanceList.each { it.updateDisplaySrc()}
		params.messageSection = 'inbox'
		[messageInstanceList: messageInstanceList,
				messageSection: 'inbox',
				messageInstanceTotal: messageInstanceList.size()] << show(messageInstanceList)
	}

    def sent = {
		params.inbound = false
		[messageSection: 'sent']
    }

	def poll = {
		def ownerInstance = Poll.get(params.ownerId)
		def messageInstanceList = ownerInstance.messages
		messageInstanceList.each { it.updateDisplaySrc() }

		params.messageSection = 'poll'
		[messageInstanceList: messageInstanceList,
				messageSection: 'poll',
				messageInstanceTotal: messageInstanceList.size(),
				ownerInstance: ownerInstance,
				responseList: ownerInstance.responseStats] << show(messageInstanceList)
	}
	
	def folder = {
		def ownerInstance = Folder.get(params.ownerId)
		def messageInstanceList = Fmessage.getFolderMessages(params.ownerId)
		messageInstanceList.each{ it.updateDisplaySrc() }

		if(params.flashMessage) { flash.message = params.flashMessage }

		params.messageSection = 'folder'
		[messageInstanceList: messageInstanceList,
				messageSection: 'folder',
				messageInstanceTotal: messageInstanceList.size(),
				ownerInstance: ownerInstance] << show(messageInstanceList)
	}

	def move = {
		withFmessage { messageInstance ->
			def messageOwner
			if(params.messageSection == 'poll') {
				messageOwner = Poll.get(params.ownerId)
			} else if (params.messageSection == 'folder') {
				messageOwner = Folder.get(params.ownerId)
			}
			if(messageOwner instanceof Poll) {
				def unknownResponse = messageOwner.getResponses().find { it.value == 'Unknown'}
				unknownResponse.addToMessages(Fmessage.get(params.messageId)).save(failOnError: true, flush: true)
			} else if (messageOwner instanceof Folder) {
				messageOwner.addToMessages(Fmessage.get(params.messageId)).save(failOnError: true, flush: true)
			}
			flash.message = "${message(code: 'default.updated.message', args: [message(code: 'message.label', default: 'Fmessage'), messageInstance.id])}"
			redirect(action: params.messageSection, params: params)
		}
	}

	def changeResponse = {
		withFmessage { messageInstance ->
			def responseInstance = PollResponse.get(params.responseId)
			responseInstance.addToMessages(messageInstance).save(failOnError: true, flush: true)
			flash.message = "${message(code: 'default.updated.message', args: [message(code: 'message.label', default: 'Fmessage'), messageInstance.id])}"
			redirect(action: "poll", params: params)
		}
	}
	
	def deleteMessage = {
		withFmessage { messageInstance ->
			messageInstance.toDelete()
			messageInstance.save(failOnError: true, flush: true)
			Fmessage.get(params.messageId).messageOwner?.refresh()
			flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'message.label', default: 'Fmessage'), messageInstance.id])}"
            params.remove('messageId')
			redirect(action: params.messageSection, params:params)
		}
	}

	private def withFmessage(Closure c) {
		def m = Fmessage.get(params.messageId)
		if(m) c.call(m)
		else render(text: "Could not find message with id ${params.messageId}") // TODO handle error state properly
	}
}
