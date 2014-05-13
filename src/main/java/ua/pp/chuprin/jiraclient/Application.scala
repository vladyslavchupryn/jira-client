package ua.pp.chuprin.jiraclient

import java.io.FileInputStream
import java.util.Properties
import java.net.URI
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import com.atlassian.jira.rest.client.domain.input._
import com.atlassian.jira.rest.client.domain.Transition
import scala.collection.JavaConversions._
import org.joda.time.DateTime

object Application extends App {
  val properties = new Properties
  properties.load(new FileInputStream("jira-client.properties"))

  val restClient = new AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(new URI(properties.getProperty("server")),
    properties.getProperty("login"), properties.getProperty("password"));

  val issueKey = args(0)
  val requestKey = args(1)
  val branch = args(2)
  val estimate = args(3)
  val logged = args(4)

// TODO not working in our jira version
//  restClient.getIssueClient().linkIssue(new LinkIssuesInput(requestKey, issueKey, "depends"))

  val issue = restClient.getIssueClient().getIssue(issueKey).claim()

  val transitions = restClient.getIssueClient().getTransitions(issue).claim()

  // TODO set estimate

  val resolveIssueTransition = getTransitionByName(transitions, "Resolve Issue")
  restClient.getIssueClient().transition(issue, new TransitionInput(
    resolveIssueTransition.getId(),
    List(new FieldInput("resolution", "Решен"))
  )).claim()

  restClient.getIssueClient().addWorklog(issue.getWorklogUri, new WorklogInputBuilder(issue.getSelf()).
    setStartDate(new DateTime).
    setMinutesSpent(logged.toInt).
    build).claim()


  def getTransitionByName(transitions : java.lang.Iterable[Transition], transitionName : String) : Transition = {
    for (transition <- transitions) {
      if (transition.getName().equals(transitionName)) {
        return transition;
      }
    }

    throw new IllegalStateException
  }
}
