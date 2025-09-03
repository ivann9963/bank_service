// src/types.ts
export type AccountStatus = 'ACTIVE' | 'FROZEN';

export interface AccountResponse {
  id: number;
  name: string;
  iban: string;
  status: AccountStatus;
  availableAmount: number;
  createdOn: string;
  modifiedOn: string;
}

export interface AccountCreateRequest {
  name: string;
  iban: string;
  initialAmount?: number;
}

export interface AccountUpdateRequest {
  name: string;
  iban: string;
  availableAmount: number;
}

export type TransferType = 'DEBIT' | 'CREDIT';

export interface TransferResponse {
  id: number;
  accountId: number;
  beneficiaryAccountId: number;
  type: TransferType;
  amount: number;
  createdOn: string;
  modifiedOn: string;
}

export interface TransferCreateRequest {
  fromAccountId: number;
  toAccountId: number;
  amount: number;
}
