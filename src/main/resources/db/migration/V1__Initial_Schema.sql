CREATE TABLE contracts (
    id BIGSERIAL PRIMARY KEY,
    contract_number VARCHAR(255) NOT NULL UNIQUE,
    customer_reference VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    contract_type VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    currency VARCHAR(10) NOT NULL,
    payment_terms VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    gross_value NUMERIC(19, 2),
    net_value NUMERIC(19, 2),
    parent_contract_id BIGINT,
    approved_by VARCHAR(255),
    approved_at TIMESTAMP WITH TIME ZONE,
    termination_reason TEXT,
    termination_date TIMESTAMP WITH TIME ZONE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_parent_contract FOREIGN KEY (parent_contract_id) REFERENCES contracts(id),
    CONSTRAINT chk_end_after_start CHECK (end_date > start_date)
);

CREATE TABLE contract_line_items (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    product_code VARCHAR(255) NOT NULL,
    description TEXT,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(19, 2) NOT NULL,
    discount_percentage NUMERIC(5, 2) DEFAULT 0,
    line_total NUMERIC(19, 2) NOT NULL,
    CONSTRAINT fk_contract_line_item FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE,
    CONSTRAINT chk_quantity CHECK (quantity >= 1),
    CONSTRAINT chk_unit_price CHECK (unit_price >= 0),
    CONSTRAINT chk_discount CHECK (discount_percentage >= 0 AND discount_percentage <= 40)
);

CREATE TABLE contract_approvals (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    approver_name VARCHAR(255) NOT NULL,
    approved_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remarks TEXT,
    CONSTRAINT fk_contract_approval FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE
);

CREATE INDEX idx_contracts_status ON contracts(status);
CREATE INDEX idx_contracts_customer_ref ON contracts(customer_reference);
CREATE INDEX idx_contracts_dates ON contracts(start_date, end_date);
CREATE INDEX idx_contract_line_items_contract_id ON contract_line_items(contract_id);
CREATE INDEX idx_contract_approvals_contract_id ON contract_approvals(contract_id);
