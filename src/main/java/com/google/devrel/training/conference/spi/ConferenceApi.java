package com.google.devrel.training.conference.spi;

import static com.google.devrel.training.conference.service.OfyService.ofy;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;
import com.google.devrel.training.conference.Constants;
import com.google.devrel.training.conference.domain.Profile;
import com.google.devrel.training.conference.form.ProfileForm;
import com.google.devrel.training.conference.form.ProfileForm.TeeShirtSize;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Defines conference APIs.
 */
@Api(name = "conference", version = "v1", scopes = { Constants.EMAIL_SCOPE }, clientIds = { Constants.WEB_CLIENT_ID,
        Constants.API_EXPLORER_CLIENT_ID }, description = "API for the Conference Central Backend application.")
public class ConferenceApi {

    /*
     * Get the display name from the user's email. For example, if the email is
     * lemoncake@example.com, then the display name becomes "lemoncake."
     */
    private static String extractDefaultDisplayNameFromEmail(String email) {
        return email == null ? null : email.substring(0, email.indexOf("@"));
    }

    /**
     * Creates or updates a Profile object associated with the given user
     * object.
     *
     * @param user
     *            A User object injected by the cloud endpoints.
     * @param profileForm
     *            A ProfileForm object sent from the client form.
     * @return Profile object just created.
     * @throws UnauthorizedException
     *             when the User object is null.
     */

    @ApiMethod(name = "saveProfile", path = "profile", httpMethod = HttpMethod.POST, clientIds = {
            Constants.WEB_CLIENT_ID })
    public Profile saveProfile(ProfileForm profileForm, User user) throws UnauthorizedException {

        String userId;
        String mainEmail;
        String displayName;
        TeeShirtSize teeShirtSize;
        if (user == null) {
            throw new UnauthorizedException("Forbidden");
        }

        teeShirtSize = profileForm.getTeeShirtSize();
        displayName = profileForm.getDisplayName();
        userId = user.getUserId();
        mainEmail = user.getEmail();
        if (displayName == null || displayName.isEmpty()) {
            displayName = extractDefaultDisplayNameFromEmail(mainEmail);
        }

        Profile profile = getProfile(user);
        if (profile == null) {
            profile = new Profile(userId, displayName, mainEmail, teeShirtSize);
        } else {
            profile.update(displayName, teeShirtSize);
        }

        return profile;
    }

    /**
     * @param user
     *            A User object injected by the cloud endpoints.
     * @return Profile object.
     * @throws UnauthorizedException
     *             when the User object is null.
     */
    @ApiMethod(name = "getProfile", path = "profile", httpMethod = HttpMethod.GET)
    public Profile getProfile(final User user) throws UnauthorizedException {
        if (user == null) {
            throw new UnauthorizedException("Authorization required");
        }
        String userId = user.getUserId();
        Key<Profile> key = Key.create(Profile.class, userId);
        Profile profile = ofy().load().key(key).now();
        return profile;
    }

    // @ApiMethod(name = "getProfile", path = "profile", httpMethod = HttpMethod.GET, clientIds = {Constants.WEB_CLIENT_ID})
    // public Profile getProfile(final User user) throws UnauthorizedException {
    //     if (user == null) {
    //         throw new UnauthorizedException("Authorization required");
    //     }

    //     // TODO
    //     // load the Profile Entity
    //     String userId = ""; // TODO
    //     Key key = null; // TODO
    //     Profile profile = null; // TODO load the Profile entity
    //     return profile;
    // }
}