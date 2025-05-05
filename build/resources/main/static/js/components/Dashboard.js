const Dashboard = ({ user, onLogout }) => {
    if (!user) {
        return (
            <div className="container mt-5">
                <div className="alert alert-danger">
                    <h4>Authentication Error</h4>
                    <p>You are not logged in. Please <a href="#" onClick={() => window.location.href = '/login'}>login</a> to continue.</p>
                </div>
            </div>
        );
    }
    
    return (
        <>
            <header className="dashboard-header">
                <div className="container">
                    <div className="d-flex justify-content-between align-items-center">
                        <h2 className="m-0">Learning Media</h2>
                        <div className="d-flex align-items-center">
                            <span className="me-3">Welcome, {user.firstName} {user.lastName}</span>
                            <button className="btn btn-outline-light" onClick={onLogout}>Logout</button>
                        </div>
                    </div>
                </div>
            </header>
            
            <div className="container dashboard-content">
                <div className="row">
                    <div className="col-12">
                        <div className="alert alert-success welcome-alert">
                            <h4>Welcome to Your Dashboard!</h4>
                            <p>You've successfully logged in to your Learning Media account.</p>
                        </div>
                    </div>
                </div>
                
                <div className="row">
                    <div className="col-md-4">
                        <div className="card user-card">
                            <div className="card-header bg-primary text-white">
                                <h5 className="m-0">User Profile</h5>
                            </div>
                            <div className="card-body">
                                <div className="mb-3 text-center">
                                    <div className="bg-light rounded-circle d-inline-flex justify-content-center align-items-center mb-3" style={{ width: '100px', height: '100px' }}>
                                        <span className="display-4">{user.firstName.charAt(0)}{user.lastName.charAt(0)}</span>
                                    </div>
                                    <h4>{user.firstName} {user.lastName}</h4>
                                    <p className="text-muted">{user.email}</p>
                                </div>
                                <ul className="list-group list-group-flush mb-3">
                                    <li className="list-group-item d-flex justify-content-between">
                                        <span>Account Status:</span>
                                        <span className="badge bg-success">Active</span>
                                    </li>
                                    <li className="list-group-item d-flex justify-content-between">
                                        <span>Member Since:</span>
                                        <span>{new Date().toLocaleDateString()}</span>
                                    </li>
                                </ul>
                                <button className="btn btn-outline-primary w-100">Edit Profile</button>
                            </div>
                        </div>
                    </div>
                    
                    <div className="col-md-8">
                        <div className="card user-card">
                            <div className="card-header bg-primary text-white">
                                <h5 className="m-0">My Activities</h5>
                            </div>
                            <div className="card-body">
                                <p className="text-muted">No recent activities to display.</p>
                                <div className="d-grid gap-2">
                                    <button className="btn btn-outline-primary">Explore Courses</button>
                                    <button className="btn btn-outline-secondary">Browse Resources</button>
                                </div>
                            </div>
                        </div>
                        
                        <div className="card user-card mt-4">
                            <div className="card-header bg-primary text-white">
                                <h5 className="m-0">Connected Accounts</h5>
                            </div>
                            <div className="card-body">
                                <div className="d-flex align-items-center mb-3">
                                    <img src="https://upload.wikimedia.org/wikipedia/commons/5/53/Google_%22G%22_Logo.svg" 
                                        alt="Google" style={{ width: '24px', height: '24px', marginRight: '10px' }} />
                                    <div>
                                        <strong>Google</strong>
                                        <div className="text-muted small">
                                            {user.oauthProviders && user.oauthProviders.includes('google') 
                                                ? 'Connected' 
                                                : 'Not connected'}
                                        </div>
                                    </div>
                                    <div className="ms-auto">
                                        {user.oauthProviders && user.oauthProviders.includes('google') 
                                            ? <span className="badge bg-success">Connected</span> 
                                            : <button className="btn btn-sm btn-outline-primary">Connect</button>}
                                    </div>
                                </div>
                                
                                <div className="d-flex align-items-center">
                                    <img src="https://upload.wikimedia.org/wikipedia/commons/5/51/Facebook_f_logo_%282019%29.svg" 
                                        alt="Facebook" style={{ width: '24px', height: '24px', marginRight: '10px' }} />
                                    <div>
                                        <strong>Facebook</strong>
                                        <div className="text-muted small">
                                            {user.oauthProviders && user.oauthProviders.includes('facebook') 
                                                ? 'Connected' 
                                                : 'Not connected'}
                                        </div>
                                    </div>
                                    <div className="ms-auto">
                                        {user.oauthProviders && user.oauthProviders.includes('facebook') 
                                            ? <span className="badge bg-success">Connected</span> 
                                            : <button className="btn btn-sm btn-outline-primary">Connect</button>}
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}; 