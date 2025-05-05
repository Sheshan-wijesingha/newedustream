const App = () => {
    const [currentPage, setCurrentPage] = React.useState('login');
    const [user, setUser] = React.useState(null);
    const [isLoading, setIsLoading] = React.useState(true);
    
    // Check if user is already logged in
    React.useEffect(() => {
        const checkAuth = async () => {
            try {
                const response = await fetch('/api/user/me');
                
                if (response.ok) {
                    const userData = await response.json();
                    setUser(userData);
                    setCurrentPage('dashboard');
                }
            } catch (error) {
                console.error('Auth check failed:', error);
            } finally {
                setIsLoading(false);
            }
        };
        
        checkAuth();
    }, []);
    
    const handleLogout = async () => {
        try {
            await fetch('/logout', { method: 'POST' });
            setUser(null);
            setCurrentPage('login');
        } catch (error) {
            console.error('Logout failed:', error);
        }
    };
    
    // Render loading state
    if (isLoading) {
        return (
            <div className="d-flex justify-content-center align-items-center" style={{ height: '100vh' }}>
                <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Loading...</span>
                </div>
            </div>
        );
    }
    
    // Determine which page to show
    let content;
    switch (currentPage) {
        case 'login':
            content = <Login onNavigate={setCurrentPage} onLogin={setUser} />;
            break;
        case 'register':
            content = <Register onNavigate={setCurrentPage} />;
            break;
        case 'dashboard':
            content = <Dashboard user={user} onLogout={handleLogout} />;
            break;
        default:
            content = <Login onNavigate={setCurrentPage} onLogin={setUser} />;
    }
    
    return (
        <div className="app-container">
            {content}
        </div>
    );
}; 