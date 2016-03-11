package scorex.perma.storage

import org.mapdb.{HTreeMap, Serializer}
import scorex.crypto.storage.MapDBStorage
import scorex.crypto.storage.auth.AuthDataBlock
import scorex.perma.settings.PermaConstants.{DataSegment, DataSegmentIndex}

class AuthDataStorage(fileName: String) extends MapDBStorage[DataSegmentIndex, AuthDataBlock[DataSegment]](fileName) {

  override protected val map: HTreeMap[DataSegmentIndex, AuthDataBlock[DataSegment]] = db.hashMapCreate("segments")
    .keySerializer(Serializer.LONG)
    .makeOrGet()

}
