/*
 * Copyright (C) 2013 Open WhisperSystems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.whispersystems.textsecuregcm.storage;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import org.whispersystems.textsecuregcm.auth.AmbiguousIdentifier;
import org.whispersystems.textsecuregcm.auth.StoredRegistrationLock;

import javax.security.auth.Subject;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AccountCoinBalance {

  @JsonIgnore
  private String uuid;

  @JsonProperty
  private String coin_name;

  @JsonProperty
  private String balance;

  @JsonProperty
  private String coin_status;

  @JsonProperty
  private String recharge_address;

  @JsonProperty
  private String confirming_balance;

  @JsonProperty
  private String symbol;
  @JsonProperty
  private String period;
  @JsonProperty
  private String percent;

  public String getPercent() {
    return percent;
  }

  public void setPercent(String percent) {
    this.percent = percent;
  }

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  public String getPeriod() {
    return period;
  }

  public void setPeriod(String period) {
    this.period = period;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getCoin_name() {
    return coin_name;
  }

  public void setCoin_name(String coin_name) {
    this.coin_name = coin_name;
  }

  public String getBalance() {
    return balance;
  }

  public void setBalance(String balance) {
    this.balance = balance;
  }

  public String getCoin_status() {
    return coin_status;
  }

  public void setCoin_status(String coin_status) {
    this.coin_status = coin_status;
  }

  public String getRecharge_address() {
    return recharge_address;
  }

  public void setRecharge_address(String recharge_address) {
    this.recharge_address = recharge_address;
  }

  public String getConfirming_balance() {
    return confirming_balance;
  }

  public void setConfirming_balance(String confirming_balance) {
    this.confirming_balance = confirming_balance;
  }
}
