/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.agentstatuschange.services

import com.google.inject.Singleton
import javax.inject.Inject
import org.joda.time.DateTime
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import uk.gov.hmrc.agentstatuschange.models.AgentStatusChangeRecord
import uk.gov.hmrc.agentstatuschange.repository.StrictlyEnsureIndexes
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.collection.Seq
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentStatusChangeMongoService @Inject()(
    mongoComponent: ReactiveMongoComponent)
    extends ReactiveRepository[AgentStatusChangeRecord, BSONObjectID](
      "agent-status-change",
      mongoComponent.mongoConnector.db,
      AgentStatusChangeRecord.format,
      ReactiveMongoFormats.objectIdFormats)
    with StrictlyEnsureIndexes[AgentStatusChangeRecord, BSONObjectID] {

  override def indexes: Seq[Index] =
    Seq(
      Index(
        Seq("arn" -> IndexType.Ascending, "lastUpdated" -> IndexType.Ascending),
        Some("Arn_LastUpdated")
      )
    )

  implicit val ord: Ordering[DateTime] =
    Ordering.by(time => time.getMillis)

  def findCurrentRecordByArn(arn: String)(implicit ec: ExecutionContext)
    : Future[Option[AgentStatusChangeRecord]] = {

    val selector = Json.obj("arn" -> Json.toJson(arn))
    val descending = -1
    val sort = Json.obj("lastUpdated" -> JsNumber(descending))

    collection
      .find(selector, projection = None)
      .sort(sort)
      .one[AgentStatusChangeRecord]
  }

  def createRecord(agentStatusChangeRecord: AgentStatusChangeRecord)(
      implicit ec: ExecutionContext): Future[Unit] =
    insert(agentStatusChangeRecord).map(_ => ())
}
