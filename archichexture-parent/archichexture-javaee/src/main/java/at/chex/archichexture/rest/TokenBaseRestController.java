package at.chex.archichexture.rest;

import at.chex.archichexture.dto.BaseDto;
import at.chex.archichexture.model.BaseEntity;
import at.chex.archichexture.rest.token.TokenCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * @param <ENTITY>
 * @param <DTO>
 *
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 27/03/2017
 */
public abstract class TokenBaseRestController<ENTITY extends BaseEntity, DTO extends BaseDto<ENTITY>> extends BaseRestController<ENTITY, DTO> {
    private static final Logger log = LoggerFactory.getLogger(BaseRestController.class);
    private TokenCheck tokenCheck;
    private boolean readonlyController = true;

    /**
     * Ensure, that one of the init methods is called before handling any requests
     *
     * @param tokenCheck
     */
    public void init(TokenCheck tokenCheck) {
        this.init(tokenCheck, true);
    }

    /**
     * Ensure, that one of the init methods is called before handling any requests
     *
     * @param tokenCheck
     * @param readonlyController
     */
    public void init(TokenCheck tokenCheck, boolean readonlyController) {
        super.init();
        this.tokenCheck = tokenCheck;
        this.readonlyController = readonlyController;
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response executeGetListRequest(
            @Context UriInfo info,
            @DefaultValue("50") @QueryParam("limit") int limit,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @QueryParam(value = "reset_token") @DefaultValue("true") boolean resetTokenTimes,
            @QueryParam(value = "token") String token) {

        if (!this.isInitialized()) {
            log.error("Uninitialized Rest Controller! Call init() before doing anything else!");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        log.trace("GET:/ with Parameters. reset_token:{}, token:{}, limit:{}, offset:{}", resetTokenTimes, token, limit, offset);

        int tokenStatusResponseCode = getTokenResponseCode(token, resetTokenTimes);
        if (tokenStatusResponseCode > 0) {
            return Response.status(tokenStatusResponseCode).build();
        }

        return internalGETListRequest(info, token, limit, offset);
    }

    /**
     * This returns the correct HTTP:ResponseCode. If 0, this is interpreted as "everything's ok". Any other value will
     * be returned as the HTTP:Result.
     *
     * @param token
     * @param resetTokenExpiration
     *
     * @return
     */
    protected int getTokenResponseCode(String token, boolean resetTokenExpiration) {
        log.debug("Checking validity of token:{}", token);
        return tokenCheck.getTokenResponseCode(token, resetTokenExpiration);
    }

    /**
     * Process the GET List Request here.
     *
     * @param info
     * @param limit
     * @param offset
     *
     * @return
     */
    protected Response internalGETListRequest(UriInfo info, String token, int limit, int offset) {
        log.trace("Incoming LIST request for token {}", token);

        return super.internalGETListRequest(info, limit, offset);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response executeGetRequest(
            @PathParam("id") Long id,
            @QueryParam(value = "reset_token") @DefaultValue("true") boolean resetTokenTimes,
            @QueryParam(value = "token") String token) {

        if (!this.isInitialized()) {
            log.error("Uninitialized Rest Controller! Call init() before doing anything else!");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        log.trace("GET:/id with Parameters. id:{}, reset_token:{}, token:{}", id, resetTokenTimes, token);

        int tokenStatusResponseCode = getTokenResponseCode(token, resetTokenTimes);
        if (tokenStatusResponseCode > 0) {
            return Response.status(tokenStatusResponseCode).build();
        }
        return internalGETRequest(id, token);
    }

    /**
     * Execute the GET Request for the given id/token
     *
     * @param id
     * @param token
     *
     * @return
     */
    protected Response internalGETRequest(Long id, String token) {
        log.trace("Incoming request for id {} with token {}", id, token);
        return super.internalGETRequest(id);
    }

    @PUT
    @Path("/")
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.APPLICATION_JSON)
    public Response executePUTRequest(
            @BeanParam DTO formParam,
            @QueryParam(value = "reset_token") @DefaultValue("true") boolean resetTokenTimes,
            @QueryParam(value = "token") String token) {

        if (!this.isInitialized()) {
            log.error("Uninitialized Rest Controller! Call init() before doing anything else!");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        if (this.readonlyController) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        int tokenStatusResponseCode = getTokenResponseCode(token, resetTokenTimes);
        if (tokenStatusResponseCode > 0) {
            return Response.status(tokenStatusResponseCode).build();
        }

        return internalExecutePUTRequest(formParam);
    }

    @POST
    @Path("/{id}")
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.APPLICATION_JSON)
    public Response executePOSTRequest(
            @PathParam("id") Long id,
            @BeanParam DTO formParam,
            @QueryParam(value = "reset_token") @DefaultValue("true") boolean resetTokenTimes,
            @QueryParam(value = "token") String token) {

        if (!this.isInitialized()) {
            log.error("Uninitialized Rest Controller! Call init() before doing anything else!");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        log.trace("POST:/id with Parameters. id:{}, dto:{}, reset_token:{}, token:{}", id, formParam, resetTokenTimes, token);

        if (this.readonlyController) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        int tokenStatusResponseCode = getTokenResponseCode(token, resetTokenTimes);
        if (tokenStatusResponseCode > 0) {
            return Response.status(tokenStatusResponseCode).build();
        }

        return internalExecutePOSTRequest(id, token, formParam);
    }

    /**
     * Process the update-entity event
     *
     * @param id
     * @param token
     * @param formParam
     *
     * @return
     */
    protected Response internalExecutePOSTRequest(Long id, String token, DTO formParam) {
        log.debug("Update entity with id {} for token {}. FormParams: {}", id, token, formParam);
        return super.internalExecutePOSTRequest(id, formParam);
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response executeDELETERequest(
            @PathParam("id") Long id,
            @QueryParam(value = "reset_token") @DefaultValue("true") boolean resetTokenTimes,
            @QueryParam(value = "token") String token) {

        if (!this.isInitialized()) {
            log.error("Uninitialized Rest Controller! Call init() before doing anything else!");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        if (this.readonlyController) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        int tokenStatusResponseCode = getTokenResponseCode(token, resetTokenTimes);
        if (tokenStatusResponseCode > 0) {
            return Response.status(tokenStatusResponseCode).build();
        }

        return internalExecuteDELETERequest(id, token, resetTokenTimes);
    }

    /**
     * Delete the entity with the given id
     *
     * @param id
     * @param token
     *
     * @return
     */
    protected Response internalExecuteDELETERequest(Long id, String token, boolean resetTokenTimes) {
        if (!this.isInitialized()) {
            log.error("Uninitialized Rest Controller! Call init() before doing anything else!");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        if (this.readonlyController) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        int tokenStatusResponseCode = getTokenResponseCode(token, resetTokenTimes);
        if (tokenStatusResponseCode > 0) {
            return Response.status(tokenStatusResponseCode).build();
        }


        log.debug("Delete entity with id {} for token {}", id, token);
        ENTITY entity = loadEntityById(id, token);
        if (this.getEntityRepository().delete(entity)) {
            return Response.ok().build();
        }
        return Response.status(Response.Status.PRECONDITION_FAILED).build();
    }

    /**
     * Override this to interfere in the loading process (e.g. load different entities according to id characteristics)
     *
     * @param id
     *
     * @return
     */
    protected ENTITY loadEntityById(Long id, String token) {
        return super.loadEntityById(id);
    }
}
