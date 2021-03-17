package org.whispersystems.textsecuregcm.auth;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import io.dropwizard.auth.basic.BasicCredentials;
import org.signal.zkgroup.auth.ServerZkAuthOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.textsecuregcm.storage.GroupEntity;
import org.whispersystems.textsecuregcm.storage.GroupsManager;
import org.whispersystems.textsecuregcm.storage.Device;
import org.whispersystems.textsecuregcm.util.Constants;
import org.whispersystems.textsecuregcm.util.Util;

import java.util.Optional;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * lxr 20210316
 */
public class BaseGroupAuthenticator {

    private final MetricRegistry metricRegistry = SharedMetricRegistries.getOrCreate(Constants.METRICS_NAME);
    private final Meter authenticationFailedMeter = metricRegistry.meter(name(getClass(), "authentication", "failed"));
    private final Meter authenticationSucceededMeter = metricRegistry.meter(name(getClass(), "authentication", "succeeded"));
    private final Meter noSuchGroupMeter = metricRegistry.meter(name(getClass(), "authentication", "noSuchGroup"));
    private final Meter noSuchDeviceMeter = metricRegistry.meter(name(getClass(), "authentication", "noSuchDevice"));
    private final Meter groupDisabledMeter = metricRegistry.meter(name(getClass(), "authentication", "groupDisabled"));
    private final Meter deviceDisabledMeter = metricRegistry.meter(name(getClass(), "authentication", "deviceDisabled"));
    private final Meter invalidAuthHeaderMeter = metricRegistry.meter(name(getClass(), "authentication", "invalidHeader"));

    private final Logger logger = LoggerFactory.getLogger(GroupAuthenticator.class);

    private final GroupsManager groupsManager;
    private final ServerZkAuthOperations zkAuthOperations;

    public BaseGroupAuthenticator(GroupsManager groupsManager, ServerZkAuthOperations zkAuthOperations) {
        this.groupsManager = groupsManager;
        this.zkAuthOperations = zkAuthOperations;
    }

    public Optional<GroupEntity> authenticate(BasicCredentials basicCredentials, boolean enabledRequired) {
        try {
            AuthorizationGroupHeader authorizationHeader = AuthorizationGroupHeader.fromUserAndPassword(zkAuthOperations, basicCredentials.getUsername(), basicCredentials.getPassword());
            GroupEntity group = authorizationHeader.getGroupEntity();

            GroupEntity g = group;

//      Optional<Device> device = group.get().getDevice(authorizationHeader.getDeviceId());

//      if (!device.isPresent()) {
//        noSuchDeviceMeter.mark();
//        return Optional.empty();
//      }

            if (enabledRequired) {
//        if (!device.get().isEnabled()) {
//          deviceDisabledMeter.mark();
//          return Optional.empty();
//        }

//        if (!group.get().isEnabled()) {
//          groupDisabledMeter.mark();
//          return Optional.empty();
//        }
            }
            Optional<GroupEntity> opt = Optional.of(group);
            //if (device.get().getAuthenticationCredentials().verify(basicCredentials.getPassword())) {
            authenticationSucceededMeter.mark();
            //group.get().setAuthenticatedDevice(device.get());
//        updateLastSeen(group.get(), device.get());
            return opt;
            // }

            // authenticationFailedMeter.mark();
            //return Optional.empty();
        } catch (IllegalArgumentException | InvalidAuthorizationHeaderException iae) {
            invalidAuthHeaderMeter.mark();
            return Optional.empty();
        }
    }

    private void updateLastSeen(GroupEntity group, Device device) {
        if (device.getLastSeen() != Util.todayInMillis()) {
            device.setLastSeen(Util.todayInMillis());
            groupsManager.update(group);
        }
    }

}
