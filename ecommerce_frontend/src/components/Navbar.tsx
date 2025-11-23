import React from 'react';
import { Form, FormControl, InputGroup } from 'react-bootstrap';

const Navbar: React.FC = () => {
    return (
        <nav className="navbar navbar-light bg-white px-4 py-3 border-bottom">
            <div className="flex-grow-1 me-3">
                <Form>
                    <InputGroup>
                        <InputGroup.Text>
                            <i className="bi bi-search" />
                        </InputGroup.Text>
                        <FormControl placeholder="Search orders, customers, products..." />
                    </InputGroup>
                </Form>
            </div>

            <div className="d-flex align-items-center gap-3">
                <button className="btn btn-link p-0">
                    <i className="bi bi-sun" />
                </button>

                <button className="btn btn-link p-0 position-relative">
                    <i className="bi bi-bell" />
                    <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">
            4
          </span>
                </button>

                <div className="d-flex align-items-center">
                    <img
                        src="https://ui-avatars.com/api/?name=Admin+User&background=4F46E5&color=fff"
                        alt="avatar"
                        className="rounded-circle me-2"
                        width={36}
                        height={36}
                    />
                    <div className="d-none d-md-block">
                        <div className="fw-semibold small">Admin User</div>
                        <div className="text-muted small">E-commerce admin</div>
                    </div>
                </div>
            </div>
        </nav>
    );
};

export default Navbar;
