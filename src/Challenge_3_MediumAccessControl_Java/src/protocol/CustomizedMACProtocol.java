package protocol;


import java.util.*;

/**
 * Created by martijn.slot on 02/03/2017.
 */
public class CustomizedMACProtocol implements IMACProtocol {

    private int NUMBER_OF_PROTOCOLS = 4;
    private int protocolID = new Random().nextInt(1000);
    private List<Integer> protocolIDParticipants = new ArrayList<>();
    private int counter = -1;

    @Override
    public TransmissionInfo TimeslotAvailable(MediumState previousMediumState, int controlInformation, int localQueueLength) {

        if (protocolIDParticipants.size() != NUMBER_OF_PROTOCOLS) {
            if (previousMediumState == MediumState.Succes) {
                protocolIDParticipants.add(controlInformation);
            }
            if (!protocolIDParticipants.contains(protocolID) && new Random().nextInt(100) < 35) {
                System.out.println("SLOT - Sending NODATA to distribute protocolIDs.");
                return new TransmissionInfo(TransmissionType.NoData, protocolID);
            } else {
                System.out.println("SLOT - Silent.");
                return new TransmissionInfo(TransmissionType.Silent, 0);
            }
        }

        counter = (counter + 1) % NUMBER_OF_PROTOCOLS;

        if (protocolID == protocolIDParticipants.get(counter) && localQueueLength > 0) {
            System.out.println("SLOT - Sending data and hope for no collision.");
            return new TransmissionInfo(TransmissionType.Data, 0);
        } else {
            System.out.println("SLOT - another protocol is sending data, I am silent.");
            return new TransmissionInfo(TransmissionType.Silent, 0);
        }
    }
}
