package smartit_task.bank_service.mapper;

import smartit_task.bank_service.dto.AccountResponse;
import smartit_task.bank_service.entity.Account;

import java.util.List;

public class AccountMapper {
    private AccountMapper() {}

    public static AccountResponse toResponse(Account e) {
        AccountResponse dto = new AccountResponse();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setIban(e.getIban());
        dto.setStatus(e.getStatus());
        dto.setAvailableAmount(e.getAvailableAmount());
        dto.setCreatedOn(e.getCreatedOn());
        dto.setModifiedOn(e.getModifiedOn());
        return dto;
    }

    public static List<AccountResponse> toResponseList(List<Account> list) {
        return list.stream().map(AccountMapper::toResponse).toList();
    }
}
