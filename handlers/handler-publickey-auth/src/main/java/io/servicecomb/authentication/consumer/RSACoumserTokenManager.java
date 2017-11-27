package io.servicecomb.authentication.consumer;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.authentication.RSAAuthenticationToken;
import io.servicecomb.foundation.common.utils.RSAUtils;
import io.servicecomb.foundation.token.RSAKeypair4Auth;
import io.servicecomb.serviceregistry.RegistryUtils;

public class RSACoumserTokenManager {

  private static final Logger logger = LoggerFactory.getLogger(RSACoumserTokenManager.class);

  private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

  private RSAAuthenticationToken token;

  public String getToken() {
    readWriteLock.readLock().lock();
    if (isvalid(token)) {
      String tokenStr = token.format();
      readWriteLock.readLock().unlock();
      return tokenStr;
    } else {
      readWriteLock.readLock().unlock();
      return createToken();
    }
  }

  public String createToken() {
    PrivateKey privateKey = RSAKeypair4Auth.INSTANCE.getPrivateKey();
    readWriteLock.writeLock().lock();
    if (isvalid(token)) {
      logger.debug("Token had been recreated by another thread");
      return token.format();
    }
    String instanceId = RegistryUtils.getMicroserviceInstance().getInstanceId();
    String serviceId = RegistryUtils.getMicroservice().getServiceId();
    String randomCode = RandomStringUtils.randomAlphanumeric(128);
    long generateTime = System.currentTimeMillis();
    try {
      String plain = String.format("%s@%s@%s@%s", instanceId, serviceId, generateTime, randomCode);
      String sign = RSAUtils.sign(plain, privateKey);
      token = RSAAuthenticationToken.fromStr(String.format("%s@%s", plain, sign));
      return token.format();
    } catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | SignatureException e) {
      throw new Error("create token error");
    }

  }

  /**
   * the TTL of Token is  24 hours
   * client token will expired 15 minutes early 
   */
  public boolean isvalid(RSAAuthenticationToken token) {
    if (null == token) {
      return false;
    }
    long generateTime = token.getGenerateTime();
    Date expiredDate = new Date(generateTime + RSAAuthenticationToken.TOKEN_ACTIVE_TIME - 15 * 60 * 1000);
    Date now = new Date();
    if (now.before(expiredDate)) {
      return true;
    }
    return false;
  }


}
