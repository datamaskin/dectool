package iix.util

import iix.util.DecToolCommand
import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment

import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import java.io.ByteArrayOutputStream
import java.io.PrintStream

class HelloWorldCommandSpec extends Specification {

    @Shared @AutoCleanup ApplicationContext ctx = ApplicationContext.run(Environment.CLI, Environment.TEST)

    void "test hello-world with command line option"() {
        given:
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setOut(new PrintStream(baos))

        String[] args = ['-e TEST -from MVR -ora_messenger ../../../../../../utils/ora_messenger.xml -v'] as String[]
        PicocliRunner.run(DecToolCommand, ctx, args)

        expect:
        baos.toString().contains('Hi!')
    }

    /*void "Test parseOraMessenger for correct DB connection using default command line args"() {
        given:
        DecToolCommand hwc = new DecToolCommand();
        File f = new File("file://c:/utils/ora_messenger.xml");
        OraMessengerBean omb = hwc.parseOraMessenger(f)
    }*/
}
