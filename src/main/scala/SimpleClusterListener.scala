package com.ziyasal.peksimet

import akka.actor.{Props, UntypedActor}
import akka.cluster.ClusterEvent.{MemberRemoved, MemberUp, UnreachableMember}
import akka.cluster.{Cluster, ClusterEvent}
import akka.event.Logging


object SimpleClusterListener {
  def props: Props = Props.create(classOf[SimpleClusterListener])
}

class SimpleClusterListener extends UntypedActor {
  private[akka] val log = Logging.getLogger(getContext.system, this)
  private[akka] val cluster = Cluster.get(getContext.system)

  override def preStart(): Unit = {
    cluster.subscribe(getSelf, ClusterEvent.initialStateAsEvents,
      classOf[ClusterEvent.MemberEvent], classOf[ClusterEvent.UnreachableMember])
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(getSelf)
  }

  override def onReceive(message: Any): Unit = {
    message match {
      case mUp: MemberUp =>
        log.info("Member is Up: {}", mUp.member)
      case _ => message match {
        case mUnreachable: UnreachableMember =>
          log.info("Member detected as unreachable: {}", mUnreachable.member)
        case _ => message match {
          case mRemoved: MemberRemoved =>
            log.info("Member is Removed: {}", mRemoved.member)
          case _: ClusterEvent.MemberEvent =>

          case _ => unhandled(message)
        }
      }
    }
  }
}
