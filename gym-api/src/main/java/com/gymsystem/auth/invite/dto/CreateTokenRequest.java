//
package com.gymsystem.auth.invite.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/** Admin request to create a signup token. */
@Data
public class CreateTokenRequest {
    /** Optional: days until expiration (null = no expiry). */
    @Min(1) @Max(365)
    private Integer expiresInDays;

    /** Uses allowed (1 = single-use; >1 = multi-use). */
    @Min(1) @Max(1000)
    private Integer maxUses;
}
