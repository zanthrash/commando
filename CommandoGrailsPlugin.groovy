import org.springframework.web.context.request.RequestContextHolder as RCH
import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU
import org.springframework.validation.Errors
import org.codehaus.groovy.grails.validation.ConstrainedPropertyBuilder
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.BindException
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.codehaus.groovy.grails.web.metaclass.*
import org.codehaus.groovy.grails.web.binding.DataBindingUtils;
import org.codehaus.groovy.grails.plugins.DomainClassPluginSupport

class CommandoGrailsPlugin {
    def version = 0.1
    def grailsVersion = grails.util.GrailsUtil.getGrailsVersion()
    def dependsOn = [core:grailsVersion]
	def watchedResources = ["file:./grails-app/controllers/**/*Controller.groovy",
            "file:./plugins/*/grails-app/controllers/**/*Controller.groovy",
            "file:./plugins/*/grails-app/taglib/**/*TagLib.groovy",
            "file:./grails-app/taglib/**/*TagLib.groovy"]
    //def observe = ["controllers"]
    def author = "Zan Thrash"
    def authorEmail = "zan@zanthrash.com"
    def title = "Commando"
    def description = '''\
Provies a factory to create and properly wire up command objects so you don't have to
always passs as a parameter to your action.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/Commando+Plugin"

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional)
    }

    def doWithDynamicMethods = { ctx ->
        application.controllerClasses.each { controller ->
			controller.metaClass.createCommand = { commandObjectClass,params ->
				def commandObject = commandObjectClass.newInstance()
                def commandObjectMetaClass = commandObjectClass.metaClass
            
                commandObjectMetaClass.setErrors = {Errors errors ->
                    RCH.currentRequestAttributes().setAttribute("${commandObjectClass.name}_errors", errors, 0)
                }
                commandObjectMetaClass.getErrors = {->
                    RCH.currentRequestAttributes().getAttribute("${commandObjectClass.name}_errors", 0)
                }

                commandObjectMetaClass.hasErrors = {->
                    errors?.hasErrors() ? true : false
                }
                commandObjectMetaClass.validate = {->
                    DomainClassPluginSupport.validateInstance(delegate, ctx)
                }
                def validationClosure = GCU.getStaticPropertyValue(commandObjectClass, 'constraints')
                if (validationClosure) {
                    def constrainedPropertyBuilder = new ConstrainedPropertyBuilder(commandObject)
                    validationClosure.setDelegate(constrainedPropertyBuilder)
                    validationClosure()
                    commandObjectMetaClass.constraints = constrainedPropertyBuilder.constrainedProperties
                } else {
                    commandObjectMetaClass.constraints = [:]
                }

				DataBindingUtils.bindObjectToInstance(commandObject, params)
				return commandObject
			}
        }
    }

    def onChange = { event ->
        if (application.isControllerClass(event.source)){
            //addCreateCommmandToController(event.source)
		}
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }
}
