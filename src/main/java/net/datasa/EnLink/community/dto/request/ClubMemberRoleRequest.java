package net.datasa.EnLink.community.dto.request;

import lombok.Data;

@Data
public class ClubMemberRoleRequest {
	private Integer cmId;
	private String role;
	private String status;
}
